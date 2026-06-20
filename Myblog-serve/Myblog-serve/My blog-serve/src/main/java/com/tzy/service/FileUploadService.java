package com.tzy.service;

import com.tzy.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传服务
 *
 * @author tzy
 * @date 2024/04/04
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    private final Map<String, String> uploadDirMap = new HashMap<>();
    @Value("${file.upload.path.video}")
    private String VIDEO_UPLOAD_DIR;
    @Value("${file.upload.path.images}")
    private String IMAGES_UPLOAD_DIR;
    @Value("${file.upload.path.music}")
    private String MUSIC_UPLOAD_DIR;
    @Value("${file.upload.path.lrc}")
    private String LRC_UPLOAD_DIR;
    @Value("${file.upload.path.loveroom}")
    private String LOVE_ROOM_UPLOAD_DIR;
    @Value("${file.upload.path.lifetime}")
    private String LIFE_TIME_UPLOAD_DIR;

    /**
     * 初始化上传目录
     * 在服务启动时检查并创建所有必要的上传目录
     */
    @PostConstruct
    public void init() {
        initUploadDirMap();
        createUploadDirectories();
    }

    /**
     * 初始化上传目录映射
     */
    private void initUploadDirMap() {
        uploadDirMap.put("video", VIDEO_UPLOAD_DIR);
        uploadDirMap.put("images", IMAGES_UPLOAD_DIR);
        uploadDirMap.put("music", MUSIC_UPLOAD_DIR);
        uploadDirMap.put("loveroom", LOVE_ROOM_UPLOAD_DIR);
        uploadDirMap.put("lifetime", LIFE_TIME_UPLOAD_DIR);
    }

    /**
     * 创建所有必要的上传目录
     */
    private void createUploadDirectories() {
        uploadDirMap.values().forEach(dir -> {
            try {
                Path path = Paths.get(dir);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    logger.info("创建上传目录成功: {}", dir);
                }
            } catch (IOException e) {
                logger.error("创建上传目录失败: {}, 错误: {}", dir, e.getMessage());
                throw new RuntimeException("创建上传目录失败: " + dir, e);
            }
        });

        // 单独处理歌词目录
        try {
            Path lrcPath = Paths.get(LRC_UPLOAD_DIR);
            if (!Files.exists(lrcPath)) {
                Files.createDirectories(lrcPath);
                logger.info("创建歌词上传目录成功: {}", LRC_UPLOAD_DIR);
            }
        } catch (IOException e) {
            logger.error("创建歌词上传目录失败: {}, 错误: {}", LRC_UPLOAD_DIR, e.getMessage());
            throw new RuntimeException("创建歌词上传目录失败: " + LRC_UPLOAD_DIR, e);
        }
    }

    /**
     * 确保上传目录存在
     *
     * @param directory 目录路径
     * @throws IOException 创建目录失败时抛出异常
     */
    private void ensureDirectoryExists(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
                logger.info("创建目录成功: {}", directory);
            } catch (IOException e) {
                logger.error("创建目录失败: {}, 错误: {}", directory, e.getMessage());
                throw e;
            }
        }
    }

    public UploadResult uploadFile(MultipartFile file, String type, MultipartFile lyricsFile, HttpServletRequest request)
            throws IOException, NoSuchAlgorithmException {
        // 校验文件类型
        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("只允许上传图片、视频、音乐文件。");
        }

        // 根据文件类型选择上传目录
        String uploadDir = chooseUploadDirectory(type);
        if (uploadDir == null) {
            throw new IllegalArgumentException("无效的文件类型.");
        }

        // 确保目录存在
        ensureDirectoryExists(uploadDir);

        // 计算文件哈希值
        String fileHash = FileUtils.calculateFileHash(file);

        // 检查文件是否已存在（通过哈希值判断）
        String existingFilename = findExistingFileByHash(uploadDir, fileHash);
        if (existingFilename != null) {
            logger.info("文件内容已存在，跳过上传: {} -> 已存在文件: {}", file.getOriginalFilename(), existingFilename);
            // 返回已存在文件的URL
            return createUploadResult(request, existingFilename, type, fileHash, file.getOriginalFilename());
        }

        // 使用原文件名，并处理文件名冲突
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 清理文件名，移除特殊字符
        String cleanFilename = sanitizeFilename(originalFilename);

        // 生成唯一的文件名（处理冲突）
        String uniqueFilename = generateUniqueFilename(uploadDir, cleanFilename);

        // 保存文件（使用哈希值作为内部文件名，但保持原文件名的映射）
        String internalFilename = fileHash + getFileExtension(originalFilename);
        Path filePath = Paths.get(uploadDir, internalFilename);
        Files.copy(file.getInputStream(), filePath);

        // 创建文件名映射文件（原文件名 -> 内部文件名）
        createFilenameMapping(uploadDir, uniqueFilename, internalFilename);

        logger.info("文件上传成功: {} -> 内部存储: {}", uniqueFilename, internalFilename);

        // 处理歌词文件
        if ("music".equals(type) && lyricsFile != null) {
            saveLyricsFile(lyricsFile, internalFilename);
        }

        return createUploadResult(request, uniqueFilename, type, fileHash, originalFilename);
    }

    /**
     * 清理文件名，移除或替换特殊字符
     */
    private String sanitizeFilename(String filename) {
        // 移除路径分隔符和特殊字符，只保留字母、数字、下划线、中划线和点
        String cleanName = filename.replaceAll("[<>:\"/\\|?*]", "_");
        // 移除开头和结尾的空格和点
        cleanName = cleanName.trim().replaceAll("^[.\\s]+|[.\\s]+$", "");
        // 如果清理后为空，使用默认名称
        if (cleanName.isEmpty()) {
            cleanName = "uploaded_file";
        }
        return cleanName;
    }

    /**
     * 生成唯一的文件名，处理文件名冲突
     */
    private String generateUniqueFilename(String uploadDir, String filename) {
        Path filePath = Paths.get(uploadDir, filename);

        // 如果文件不存在，直接返回原文件名
        if (!Files.exists(filePath)) {
            return filename;
        }

        // 处理文件名冲突：在文件名后添加 (1), (2) 等
        String nameWithoutExt = filename;
        String extension = "";

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExt = filename.substring(0, lastDotIndex);
            extension = filename.substring(lastDotIndex);
        }

        int counter = 1;
        String newFilename;
        do {
            newFilename = nameWithoutExt + "(" + counter + ")" + extension;
            filePath = Paths.get(uploadDir, newFilename);
            counter++;
        } while (Files.exists(filePath));

        return newFilename;
    }
    
    /**
     * 检查文件内容是否重复（通过哈希值判断）
     */
    private boolean isFileContentDuplicate(String uploadDir, String fileHash) {
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                return false;
            }

            // 遍历目录中的所有文件，检查是否有相同哈希值的文件
            return Files.walk(dir, 1)
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> {
                        try {
                            // 这里可以优化：可以维护一个哈希值到文件名的映射表
                            // 或者只在需要时计算哈希值
                            return false; // 暂时返回false，避免性能问题
                        } catch (Exception e) {
                            logger.warn("检查文件哈希值时出错: {}", e.getMessage());
                            return false;
                        }
                    });
        } catch (IOException e) {
            logger.warn("检查文件重复时出错: {}", e.getMessage());
            return false;
        }
    }
    
    private void saveLyricsFile(MultipartFile lyricsFile, String musicInternalFilename) throws IOException {
        // 使用音乐文件的内部文件名（哈希值）作为歌词文件名的基础
        String nameWithoutExt = musicInternalFilename;
        int lastDotIndex = musicInternalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExt = musicInternalFilename.substring(0, lastDotIndex);
        }

        String lyricsInternalFilename = nameWithoutExt + ".lrc";
        Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsInternalFilename);
        Files.copy(lyricsFile.getInputStream(), lyricsFilePath);

        // 为歌词文件创建映射文件，使用音乐文件的原文件名
        String musicOriginalFilename = getOriginalFilenameFromMapping(getUploadDirByType("music"), musicInternalFilename);
        if (musicOriginalFilename != null && !musicOriginalFilename.equals(musicInternalFilename)) {
            createFilenameMapping(LRC_UPLOAD_DIR, musicOriginalFilename, lyricsInternalFilename);
        }

        logger.info("歌词文件保存成功: {} -> 内部存储: {}", musicOriginalFilename, lyricsInternalFilename);
    }

    private UploadResult createUploadResult(HttpServletRequest request, String uniqueFilename, String type, String fileHash, String originalFilename) {
        // 使用原文件名生成URL，但实际文件存储使用哈希值
        String fileUrl = generateFileUrl(request, uniqueFilename, type);
        UploadResult result = new UploadResult(fileUrl);
        result.setOriginalFilename(originalFilename);

        if ("music".equals(type)) {
            // 生成歌词文件名（基于音乐文件的内部文件名）
            String nameWithoutExt = fileHash;
            String lyricsFilename = nameWithoutExt + ".lrc";

            Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsFilename);
            if (Files.exists(lyricsFilePath)) {
                // 歌词文件使用专门的歌词接口，但使用音乐文件的原文件名
                String baseUrl = request.getScheme() + "://" + request.getServerName();
                if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                    baseUrl += ":" + request.getServerPort();
                }
                String lyricsFileUrl = baseUrl + "/file/lyrics/" + uniqueFilename;
                result.setLyricsUrl(lyricsFileUrl);
            }
        }

        return result;
    }

    private String chooseUploadDirectory(String type) {
        if (uploadDirMap.isEmpty()) {
            initUploadDirMap();
        }
        return uploadDirMap.get(type.toLowerCase());
    }

    /**
     * 获取指定类型的上传目录
     */
    private String getUploadDirByType(String type) {
        return chooseUploadDirectory(type);
    }
    
    private boolean isValidFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            return extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png") || extension.equals(".gif")
                || extension.equals(".mp4") || extension.equals(".avi") || extension.equals(".mkv")
                || extension.equals(".mp3") || extension.equals(".wav") || extension.equals(".flac");
        }
        return false;
    }

    private String generateFileUrl(HttpServletRequest request, String filename, String type) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }

        // 根据文件类型生成正确的访问路径
        if ("music".equals(type)) {
            return baseUrl + "/file/preview/music/" + filename;
        } else if ("video".equals(type)) {
            return baseUrl + "/file/preview/video/" + filename;
        } else if ("images".equals(type)) {
            return baseUrl + "/file/preview/images/" + filename;
        } else {
            // 默认使用通用预览接口
            return baseUrl + "/file/preview/" + type + "/" + filename;
        }
    }

    /**
     * 通过哈希值查找已存在的文件
     */
    private String findExistingFileByHash(String uploadDir, String fileHash) {
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                return null;
            }

            // 遍历目录中的所有文件，查找匹配的哈希值
            return Files.walk(dir, 1)
                    .filter(Files::isRegularFile)
                    .filter(file -> {
                        String filename = file.getFileName().toString();
                        // 检查是否是映射文件
                        if (filename.endsWith(".map")) {
                            return false;
                        }
                        // 检查文件名是否包含哈希值
                        return filename.startsWith(fileHash);
                    })
                    .findFirst()
                    .map(file -> {
                        // 检查文件是否实际存在
                        if (!Files.exists(file)) {
                            logger.warn("文件记录存在但实际文件不存在: {}", file.getFileName());
                            return null;
                        }
                        // 返回对应的原文件名
                        String internalFilename = file.getFileName().toString();
                        return getOriginalFilenameFromMapping(uploadDir, internalFilename);
                    })
                    .orElse(null);

        } catch (IOException e) {
            logger.warn("查找已存在文件时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建文件名映射文件
     */
    private void createFilenameMapping(String uploadDir, String originalFilename, String internalFilename) {
        try {
            Path mappingFile = Paths.get(uploadDir, internalFilename + ".map");
            String mappingContent = originalFilename;
            Files.write(mappingFile, mappingContent.getBytes());
            logger.debug("创建文件名映射: {} -> {}", originalFilename, internalFilename);
        } catch (IOException e) {
            logger.warn("创建文件名映射失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从映射文件中获取原文件名
     */
    private String getOriginalFilenameFromMapping(String uploadDir, String internalFilename) {
        try {
            Path mappingFile = Paths.get(uploadDir, internalFilename + ".map");
            if (Files.exists(mappingFile)) {
                return Files.readString(mappingFile).trim();
            }
        } catch (IOException e) {
            logger.warn("读取文件名映射失败: {}", e.getMessage());
        }
        // 如果没有映射文件，返回内部文件名（去掉扩展名）
        return internalFilename.substring(0, internalFilename.lastIndexOf('.'));
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    public static class UploadResult {
        private String url;
        private String lyricsUrl;
        private String originalFilename;

        public UploadResult(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLyricsUrl() {
            return lyricsUrl;
        }

        public void setLyricsUrl(String lyricsUrl) {
            this.lyricsUrl = lyricsUrl;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public void setOriginalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
        }
    }
}

package com.tzy.controller;

import com.tzy.pojo.FileRecord;
import com.tzy.service.FileRecordService;
import com.tzy.utils.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import javax.annotation.Resource;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Value("${file.upload.path.video}")
    private String VIDEO_UPLOAD_DIR;

    @Value("${file.upload.path.images}")
    private String IMAGES_UPLOAD_DIR;

    @Value("${file.upload.path.music}")
    private String MUSIC_UPLOAD_DIR;

    @Value("${file.upload.path.lrc}")
    private String LRC_UPLOAD_DIR;

    @Resource
    private FileRecordService fileRecordService;


    // 文件上传接口
    @PostMapping
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") String type,
        @RequestParam(value = "lyricsFile", required = false) MultipartFile lyricsFile,  // 可选的歌词文件
        @RequestParam(value = "preserveName", defaultValue = "false") boolean preserveName,  // 是否保留原始文件名
        HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        // 校验文件类型
        if (!isValidFileType(file)) {
            return ResponseEntity.status(400).body("只允许上传图片、视频、音乐文件。");
        }

        // 根据文件类型选择上传目录
        String uploadDir = chooseUploadDirectory(type);
        if (uploadDir == null) {
            return ResponseEntity.status(400).body("无效的文件类型.");
        }

        // 计算文件哈希值并加上扩展名
        String fileHash = FileUtils.calculateFileHash(file);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        
        String uniqueFilename;
        if (preserveName) {
            // 保留原始文件名，但添加时间戳避免冲突
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            uniqueFilename = nameWithoutExt + "_" + timestamp + extension;
        } else {
            // 使用哈希值加扩展名作为文件名
            uniqueFilename = fileHash + extension;
        }

        // 确保目录存在
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);  // 创建上传目录
            } catch (IOException e) {
                return ResponseEntity.status(500).body("创建文件夹失败: " + e.getMessage());
            }
        }

        // 创建文件路径
        Path filePath = Paths.get(uploadDir, uniqueFilename);

        // 检查文件是否已存在（通过哈希值）
        FileRecord existingRecord = fileRecordService.findByFileHash(fileHash);
        if (existingRecord != null) {
            // 检查文件是否实际存在于磁盘上
            Path existingFilePath = Paths.get(uploadDir, existingRecord.getStoredFilename());
            if (!Files.exists(existingFilePath)) {
                // 文件不存在于磁盘上，删除数据库记录并继续上传
                fileRecordService.deleteById(existingRecord.getId());
            } else {
                // 如果文件已存在，直接返回文件URL
                String fileUrl = generateFileUrl(request, existingRecord.getStoredFilename(), type);
                FileUploadResponse response = new FileUploadResponse(fileUrl);
                response.setOriginalFilename(existingRecord.getOriginalFilename());
                response.setFileSize(existingRecord.getFileSize());
                response.setFileType(existingRecord.getFileType());

                // 如果是音乐文件，检查是否存在对应的歌词文件
                if ("music".equals(type)) {
                    String lyricsFilename = existingRecord.getStoredFilename().replaceAll("\\.[^.]+$", ".lrc");
                    Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsFilename);
                    if (Files.exists(lyricsFilePath)) {
                        String lyricsFileUrl = generateFileUrl(request, lyricsFilename, "music/lrc");
                        response.setLyricsUrl(lyricsFileUrl);
                    }
                }

                // 如果是音乐文件，确保生成同名封面（若不存在则生成占位封面）
                if ("music".equals(type)) {
                    String baseName = existingRecord.getStoredFilename();
                    int dot = baseName.lastIndexOf('.');
                    if (dot > 0) baseName = baseName.substring(0, dot);
                    extractOrGenerateCover(existingFilePath, baseName);
                }

                return ResponseEntity.ok().body(response);
            }
        }

        // 检查文件是否已存在（通过文件路径）
        if (Files.exists(filePath)) {
            // 如果文件已存在，直接返回文件URL
            String fileUrl = generateFileUrl(request, uniqueFilename, type);
            FileUploadResponse response = new FileUploadResponse(fileUrl);
            response.setOriginalFilename(originalFilename);
            response.setFileSize(file.getSize());
            response.setFileType(type);

            // 如果是音乐文件，检查是否存在对应的歌词文件
            if ("music".equals(type)) {
                String lyricsFilename;
                if (preserveName) {
                    // 保留原始文件名时，歌词文件名也需要相应调整
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String lyricsOriginalName = lyricsFile != null ? lyricsFile.getOriginalFilename() : "lyrics.lrc";
                    String lyricsExtension = lyricsOriginalName != null ? lyricsOriginalName.substring(lyricsOriginalName.lastIndexOf(".")) : ".lrc";
                    String lyricsNameWithoutExt = lyricsOriginalName != null ? lyricsOriginalName.substring(0, lyricsOriginalName.lastIndexOf(".")) : "lyrics";
                    lyricsFilename = lyricsNameWithoutExt + "_" + timestamp + lyricsExtension;
                } else {
                    lyricsFilename = fileHash + ".lrc";
                }
                Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsFilename);
                if (Files.exists(lyricsFilePath)) {
                    String lyricsFileUrl = generateFileUrl(request, lyricsFilename, "music/lrc");
                    response.setLyricsUrl(lyricsFileUrl);
                }
            }

            // 如果是音乐文件，确保生成同名封面（若不存在则生成占位封面）
            if ("music".equals(type)) {
                String baseName = uniqueFilename;
                int dot = baseName.lastIndexOf('.');
                if (dot > 0) baseName = baseName.substring(0, dot);
                extractOrGenerateCover(Paths.get(MUSIC_UPLOAD_DIR, uniqueFilename), baseName);
            }

            return ResponseEntity.ok().body(response);
        }

        try {
            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 如果是音乐文件，处理歌词文件
            String lyricsFileUrl = null;
            if ("music".equals(type) && lyricsFile != null) {
                // 确保歌词文件目录存在
                Path lyricsDirPath = Paths.get(LRC_UPLOAD_DIR);
                if (!Files.exists(lyricsDirPath)) {
                    Files.createDirectories(lyricsDirPath);
                }
                
                // 根据preserveName参数决定歌词文件名
                String lyricsFilename;
                if (preserveName) {
                    // 保留原始文件名时，歌词文件名也需要相应调整
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String lyricsOriginalName = lyricsFile.getOriginalFilename();
                    String lyricsExtension = lyricsOriginalName != null ? lyricsOriginalName.substring(lyricsOriginalName.lastIndexOf(".")) : ".lrc";
                    String lyricsNameWithoutExt = lyricsOriginalName != null ? lyricsOriginalName.substring(0, lyricsOriginalName.lastIndexOf(".")) : "lyrics";
                    lyricsFilename = lyricsNameWithoutExt + "_" + timestamp + lyricsExtension;
                } else {
                    // 歌词文件使用相同的哈希值加 .lrc 后缀
                    lyricsFilename = fileHash + ".lrc";
                }
                
                Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsFilename);

                // 保存歌词文件
                Files.copy(lyricsFile.getInputStream(), lyricsFilePath);
                lyricsFileUrl = generateFileUrl(request, lyricsFilename, "music/lrc");
            }

            // 保存文件记录
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalFilename(originalFilename);
            fileRecord.setStoredFilename(uniqueFilename);
            fileRecord.setFileHash(fileHash);
            fileRecord.setFileType(type);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setFilePath(filePath.toString());
            fileRecordService.saveFileRecord(fileRecord);

            // 获取文件访问 URL
            String fileUrl = generateFileUrl(request, uniqueFilename, type);
            FileUploadResponse response = new FileUploadResponse(fileUrl);
            response.setOriginalFilename(originalFilename);
            response.setFileSize(file.getSize());
            response.setFileType(type);

            if (lyricsFileUrl != null) {
                response.setLyricsUrl(lyricsFileUrl);  // 如果有歌词文件，返回歌词文件的 URL
            }

            // 如果是音乐文件，保存后确保生成同名封面
            if ("music".equals(type)) {
                String baseName = uniqueFilename;
                int dot = baseName.lastIndexOf('.');
                if (dot > 0) baseName = baseName.substring(0, dot);
                extractOrGenerateCover(filePath, baseName);
            }

            // 返回统一的响应格式
            return ResponseEntity.ok().body(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }

    // 根据文件类型选择上传目录
    private String chooseUploadDirectory(String type) {
        switch (type.toLowerCase()) {
            case "video":
                return VIDEO_UPLOAD_DIR;
            case "images":
                return IMAGES_UPLOAD_DIR;
            case "music":
                return MUSIC_UPLOAD_DIR;
            default:
                return null;  // 无效的类型
        }
    }

    // 校验文件类型
    private boolean isValidFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            // 只允许上传图片、视频和音乐文件类型
            return extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png") || extension.equals(".gif")
                || extension.equals(".mp4") || extension.equals(".avi") || extension.equals(".mkv")
                || extension.equals(".mp3") || extension.equals(".wav") || extension.equals(".flac");
        }
        return false;
    }

    // 生成文件访问的 URL
    private String generateFileUrl(HttpServletRequest request, String filename, String type) {
        // 获取当前主机地址，避免硬编码
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        
        // 根据类型生成正确的预览URL
        if ("music/lrc".equals(type)) {
            return baseUrl + "/file/lyrics/" + filename;
        } else {
            return baseUrl + "/file/preview/" + type + "/" + filename;
        }
    }

    // 生成音乐默认封面：如果 images 目录下不存在同名图片，则生成同名 .jpg
    private void ensureCoverExists(String baseName) {
        try {
            Path coverPath = Paths.get(IMAGES_UPLOAD_DIR, baseName + ".jpg");
            if (Files.exists(coverPath)) {
                return;
            }
            // 生成简单占位封面
            int width = 300, height = 300;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(240, 243, 246));
            g.fillRect(0, 0, width, height);
            g.setColor(new Color(99, 102, 241));
            g.setFont(new Font("SansSerif", Font.BOLD, 48));
            String text = "MUSIC";
            FontMetrics fm = g.getFontMetrics();
            int tx = (width - fm.stringWidth(text)) / 2;
            int ty = (height - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, tx, ty);
            g.dispose();

            // 确保目录存在
            if (!Files.exists(Paths.get(IMAGES_UPLOAD_DIR))) {
                Files.createDirectories(Paths.get(IMAGES_UPLOAD_DIR));
            }
            ImageIO.write(image, "jpg", coverPath.toFile());
        } catch (Exception ignored) {
        }
    }

    // 优先从音乐文件内提取嵌入封面，否则生成占位封面
    private void extractOrGenerateCover(Path musicPath, String baseName) {
        try {
            Path coverPath = Paths.get(IMAGES_UPLOAD_DIR, baseName + ".jpg");
            if (Files.exists(coverPath)) return;

            // 尝试用 mp3agic 读取 MP3 的 ID3v2 封面
            if (musicPath.toString().toLowerCase().endsWith(".mp3")) {
                Mp3File mp3 = new Mp3File(musicPath.toFile());
                if (mp3.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3.getId3v2Tag();
                    byte[] imageData = id3v2Tag.getAlbumImage();
                    if (imageData != null && imageData.length > 0) {
                        if (!Files.exists(Paths.get(IMAGES_UPLOAD_DIR))) {
                            Files.createDirectories(Paths.get(IMAGES_UPLOAD_DIR));
                        }
                        Files.write(coverPath, imageData);
                        return; // 提取成功
                    }
                }
            }
        } catch (Exception ignored) {
            // 读取失败时退回到占位图
        }
        ensureCoverExists(baseName);
    }

    // 上传响应类
    public static class FileUploadResponse {
        private String url;
        private String lyricsUrl;  // 歌词文件的 URL
        private String originalFilename;  // 原始文件名
        private Long fileSize;  // 文件大小（字节）
        private String fileType;  // 文件类型

        public FileUploadResponse(String url) {
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

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
    }
}

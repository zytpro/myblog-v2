package com.tzy.controller;

import com.tzy.pojo.FileRecord;
import com.tzy.service.FileRecordService;
import com.tzy.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class OptimizedFileUploadController {

    @Autowired
    private FileRecordService fileRecordService;

    @Value("${file.upload.path.video}")
    private String VIDEO_UPLOAD_DIR;

    @Value("${file.upload.path.images}")
    private String IMAGES_UPLOAD_DIR;

    @Value("${file.upload.path.music}")
    private String MUSIC_UPLOAD_DIR;

    @Value("${file.upload.path.lrc}")
    private String LRC_UPLOAD_DIR;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "lyricsFile", required = false) MultipartFile lyricsFile,
            @RequestParam(value = "preserveName", defaultValue = "false") boolean preserveName,
            HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        if (!isValidFileType(file)) {
            return ResponseEntity.status(400).body("只允许上传图片、视频、音乐文件。");
        }

        String uploadDir = chooseUploadDirectory(type);
        if (uploadDir == null) {
            return ResponseEntity.status(400).body("无效的文件类型.");
        }

        String fileHash = FileUtils.calculateFileHash(file);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";

        // 检查是否已存在相同内容的文件
        FileRecord existingRecord = fileRecordService.findByFileHash(fileHash);
        if (existingRecord != null) {
            // 文件已存在，直接返回现有记录
            String fileUrl = generateFileUrl(request, existingRecord.getStoredFilename(), type);
            FileUploadResponse response = new FileUploadResponse(fileUrl);
            response.setFileId(existingRecord.getId());
            response.setOriginalFilename(existingRecord.getOriginalFilename());
            response.setFileSize(existingRecord.getFileSize());
            response.setUploadTime(existingRecord.getUploadTime());
            
            // 如果是音乐文件，检查歌词文件
            if ("music".equals(type)) {
                String lyricsHash = fileHash + ".lrc";
                FileRecord lyricsRecord = fileRecordService.findByFileHash(lyricsHash);
                if (lyricsRecord != null) {
                    String lyricsUrl = generateFileUrl(request, lyricsRecord.getStoredFilename(), "music/lrc");
                    response.setLyricsUrl(lyricsUrl);
                }
            }
            
            return ResponseEntity.ok().body(response);
        }

        // 生成存储文件名
        String storedFilename;
        if (preserveName) {
            // 保留原始文件名，但添加时间戳避免冲突
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            storedFilename = nameWithoutExt + "_" + timestamp + extension;
        } else {
            // 使用哈希值作为文件名
            storedFilename = fileHash + extension;
        }

        // 确保目录存在
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 保存文件
        Path filePath = Paths.get(uploadDir, storedFilename);
        Files.copy(file.getInputStream(), filePath);

        // 保存文件记录到数据库
        FileRecord fileRecord = fileRecordService.saveFileRecord(file, type, storedFilename, 
            filePath.toString(), fileHash);

        // 处理歌词文件
        String lyricsUrl = null;
        if ("music".equals(type) && lyricsFile != null) {
            String lyricsHash = fileHash + ".lrc";
            String lyricsFilename = preserveName ? 
                originalFilename.replace(extension, ".lrc") : 
                lyricsHash;
            
            Path lyricsFilePath = Paths.get(LRC_UPLOAD_DIR, lyricsFilename);
            Files.copy(lyricsFile.getInputStream(), lyricsFilePath);
            
            // 保存歌词文件记录
            FileRecord lyricsRecord = new FileRecord();
            lyricsRecord.setOriginalFilename(lyricsFile.getOriginalFilename());
            lyricsRecord.setStoredFilename(lyricsFilename);
            lyricsRecord.setFileHash(lyricsHash);
            lyricsRecord.setFileType("music/lrc");
            lyricsRecord.setFilePath(lyricsFilePath.toString());
            lyricsRecord.setFileSize(lyricsFile.getSize());
            lyricsRecord.setMimeType(lyricsFile.getContentType());
            lyricsRecord.setUploadTime(LocalDateTime.now());
            lyricsRecord.setDownloadCount(0);
            fileRecordService.save(lyricsRecord);
            
            lyricsUrl = generateFileUrl(request, lyricsFilename, "music/lrc");
        }

        // 生成响应
        String fileUrl = generateFileUrl(request, storedFilename, type);
        FileUploadResponse response = new FileUploadResponse(fileUrl);
        response.setFileId(fileRecord.getId());
        response.setOriginalFilename(fileRecord.getOriginalFilename());
        response.setFileSize(fileRecord.getFileSize());
        response.setUploadTime(fileRecord.getUploadTime());
        
        if (lyricsUrl != null) {
            response.setLyricsUrl(lyricsUrl);
        }

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/info/{fileId}")
    public ResponseEntity<?> getFileInfo(@PathVariable Long fileId) {
        FileRecord fileRecord = fileRecordService.getById(fileId);
        if (fileRecord == null) {
            return ResponseEntity.status(404).body("文件不存在");
        }
        
        return ResponseEntity.ok().body(fileRecord);
    }

    @PostMapping("/download/{fileId}")
    public ResponseEntity<?> recordDownload(@PathVariable Long fileId) {
        fileRecordService.incrementDownloadCount(fileId);
        return ResponseEntity.ok().body("下载记录已更新");
    }

    private String chooseUploadDirectory(String type) {
        switch (type.toLowerCase()) {
            case "video":
                return VIDEO_UPLOAD_DIR;
            case "images":
                return IMAGES_UPLOAD_DIR;
            case "music":
                return MUSIC_UPLOAD_DIR;
            default:
                return null;
        }
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
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "/");
        return baseUrl + "file/preview/" + type + "/" + filename;
    }

    public static class FileUploadResponse {
        private String url;
        private String lyricsUrl;
        private Long fileId;
        private String originalFilename;
        private Long fileSize;
        private LocalDateTime uploadTime;

        public FileUploadResponse(String url) {
            this.url = url;
        }

        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getLyricsUrl() { return lyricsUrl; }
        public void setLyricsUrl(String lyricsUrl) { this.lyricsUrl = lyricsUrl; }
        
        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getUploadTime() { return uploadTime; }
        public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    }
}


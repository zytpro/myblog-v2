package com.tzy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import com.tzy.pojo.Result;
import com.tzy.pojo.FileRecord;
import com.tzy.service.FileRecordService;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload.path.video}")
    private String videoUploadDir;

    @Value("${file.upload.path.images}")
    private String imagesUploadDir;

    @Value("${file.upload.path.music}")
    private String musicUploadDir;

    @Value("${file.upload.path.lrc}")
    private String lrcUploadDir;

    @Value("${file.upload.path.loveroom}")
    private String loveroomUploadDir;

    @Value("${file.upload.path.lifetime}")
    private String lifetimeUploadDir;

    @javax.annotation.Resource
    private FileRecordService fileRecordService;

    @GetMapping("/preview/images/{filename}")
    public ResponseEntity<Resource> previewImage(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return serveFileWithMapping(imagesUploadDir, decodedFilename, resolveMediaTypeByExtension(decodedFilename));
    }

    // 兼容旧路径：/file/preview/image/{filename}
    @GetMapping("/preview/image/{filename}")
    public ResponseEntity<Resource> previewImageCompat(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return serveFileWithMapping(imagesUploadDir, decodedFilename, resolveMediaTypeByExtension(decodedFilename));
    }

    @GetMapping("/preview/video/{filename}")
    public ResponseEntity<Resource> previewVideo(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return serveFileWithMapping(videoUploadDir, decodedFilename, resolveMediaTypeByExtension(decodedFilename));
    }

    @GetMapping("/preview/music/{filename}")
    public ResponseEntity<Resource> previewMusic(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return serveFileWithMapping(musicUploadDir, decodedFilename, resolveMediaTypeByExtension(decodedFilename));
    }

    @GetMapping("/lyrics/{filename}")
    public ResponseEntity<Resource> getLyrics(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        // 优先按同名 .lrc，找不到时尝试模糊匹配；仍无则返回 204，前端可认为无歌词
        String target = ensureLrcExtension(decodedFilename);
        Path candidate = resolveLyricsPath(target);
        if (candidate == null) {
            return ResponseEntity.noContent().build();
        }
        FileSystemResource resource = new FileSystemResource(candidate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + candidate.getFileName().toString() + "\"");
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/preview/{type}/{filename}")
    public ResponseEntity<Resource> previewGeneric(@PathVariable String type, @PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        String baseDir = chooseDir(type);
        if (baseDir == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return serveFileWithMapping(baseDir, decodedFilename, resolveMediaTypeByExtension(decodedFilename));
    }

    // 音乐封面：优先返回 images 目录下与音乐同名的图片，不存在则返回后端生成的默认占位图
    @GetMapping("/cover/music/{musicFilename}")
    public ResponseEntity<Resource> getMusicCover(@PathVariable String musicFilename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(musicFilename, "UTF-8");
        String base = decodedFilename;
        int idx = decodedFilename.lastIndexOf('.');
        if (idx > 0) base = decodedFilename.substring(0, idx);

        String[] exts = new String[]{".jpg", ".jpeg", ".png", ".webp"};
        for (String ext : exts) {
            Path p = Paths.get(imagesUploadDir, base + ext);
            if (Files.exists(p)) {
                return serveFileWithMapping(imagesUploadDir, base + ext, resolveMediaTypeByExtension(base + ext));
            }
        }

        // 兜底：返回一个简洁的占位 PNG，避免前端出现断图
        byte[] placeholder = generatePlaceholderPng(300, 300, "封面");
        ByteArrayResource resource = new ByteArrayResource(placeholder);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cover.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    private byte[] generatePlaceholderPng(int width, int height, String text) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(240, 243, 246));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(160, 174, 192));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(8, 8, width - 16, height - 16, 16, 16);
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        int tx = (width - fm.stringWidth(text)) / 2;
        int ty = (height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    // 视频缩略图：优先返回 images 目录下与视频同名的图片，不存在则返回后端生成的占位图
    @GetMapping("/cover/video/{videoFilename}")
    public ResponseEntity<Resource> getVideoCover(@PathVariable String videoFilename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(videoFilename, "UTF-8");
        final String base;
        int idx = decodedFilename.lastIndexOf('.');
        if (idx > 0) {
            base = decodedFilename.substring(0, idx);
        } else {
            base = decodedFilename;
        }

        String[] exts = new String[]{".jpg", ".jpeg", ".png", ".webp"};
        
        // 遍历所有图片文件，查找与视频相关的缩略图
        try {
            Path imagesDir = Paths.get(imagesUploadDir);
            if (Files.exists(imagesDir)) {
                return Files.list(imagesDir)
                        .filter(Files::isRegularFile)
                        .filter(file -> {
                            String filename = file.getFileName().toString();
                            // 检查文件名是否包含视频的基本名称
                            return filename.contains(base);
                        })
                        .findFirst()
                        .map(file -> {
                            try {
                                return serveFileWithMapping(imagesUploadDir, file.getFileName().toString(), resolveMediaTypeByExtension(file.getFileName().toString()));
                            } catch (IOException e) {
                                return null;
                            }
                        })
                        .orElseGet(() -> {
                            // 如果没有找到，返回占位图
                            try {
                                byte[] placeholder = generatePlaceholderPng(320, 180, "缩略图");
                                ByteArrayResource resource = new ByteArrayResource(placeholder);
                                return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"thumb.png\"")
                                        .contentType(MediaType.IMAGE_PNG)
                                        .body(resource);
                            } catch (IOException e) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                            }
                        });
            }
        } catch (IOException e) {
            // 忽略错误，返回占位图
        }

        // 返回占位图
        byte[] placeholder = generatePlaceholderPng(320, 180, "缩略图");
        ByteArrayResource resource = new ByteArrayResource(placeholder);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"thumb.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    // 列出指定类型目录下的文件，返回可访问的预览 URL 列表
    @GetMapping("/list")
    public Result<List<Map<String, String>>> listFiles(@RequestParam("type") String type, HttpServletRequest request) throws IOException {
        String baseDir = chooseDir(type);
        if (baseDir == null) {
            return Result.error("无效的类型");
        }

        Path dir = Paths.get(baseDir);
        if (!Files.exists(dir)) {
            return Result.error("未找到文件");
        }

        // 基础 URL
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());

        List<Map<String, String>> files = Files.list(dir)
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(name -> !name.endsWith(".map"))
                .map(name -> {
                    Map<String, String> m = new HashMap<>();
                    // 尝试从数据库获取原始文件名
                    String originalFilename = name;
                    try {
                        FileRecord record = fileRecordService.findByStoredFilename(name);
                        if (record != null) {
                            originalFilename = record.getOriginalFilename();
                        }
                    } catch (Exception e) {
                        // 数据库查询失败时使用存储的文件名
                    }
                    m.put("fileName", originalFilename);
                    m.put("storedFileName", name);
                    m.put("url", baseUrl + "/file/preview/" + type + "/" + name);
                    
                    // 为视频文件添加缩略图URL
                    if ("video".equals(type)) {
                        m.put("thumbnailUrl", baseUrl + "/file/cover/video/" + originalFilename);
                    }
                    
                    return m;
                })
                .collect(Collectors.toList());

        return Result.success(files);
    }

    private String chooseDir(String type) {
        String t = type == null ? "" : type.toLowerCase(Locale.ROOT);
        switch (t) {
            case "video":
                return videoUploadDir;
            case "images":
                return imagesUploadDir;
            case "music":
                return musicUploadDir;
            case "loveroom":
                return loveroomUploadDir;
            case "lifetime":
                return lifetimeUploadDir;
            case "music/lrc":
            case "lrc":
                return lrcUploadDir;
            default:
                return null;
        }
    }

    private ResponseEntity<Resource> serveFileWithMapping(String baseDir, String presentedFilename, MediaType mediaType) throws IOException {
        // 优先通过映射文件查找内部文件名
        String internalFilename = resolveInternalFilenameFromMapping(baseDir, presentedFilename);

        // 如果映射未找到，则尝试直接使用传入文件名
        if (internalFilename == null) {
            internalFilename = presentedFilename;
        }

        Path filePath = Paths.get(baseDir, internalFilename);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileSystemResource resource = new FileSystemResource(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType != null ? mediaType : MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeForContentDisposition(presentedFilename) + "\"");
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private String resolveInternalFilenameFromMapping(String baseDir, String presentedFilename) throws IOException {
        Path dir = Paths.get(baseDir);
        if (!Files.exists(dir)) {
            return null;
        }

        // 遍历所有 .map 文件，找到内容等于 presentedFilename 的映射
        try {
            return Files.list(dir)
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".map"))
                    .filter(p -> {
                        try {
                            String content = Files.readString(p).trim();
                            return presentedFilename.equals(content);
                        } catch (IOException ignored) {
                            return false;
                        }
                    })
                    .findFirst()
                    .map(p -> {
                        String name = p.getFileName().toString();
                        // 映射文件名形如: <internalFilename>.map
                        return name.substring(0, name.length() - 4);
                    })
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private String ensureLrcExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".lrc")) {
            return filename;
        }
        int dot = filename.lastIndexOf('.');
        if (dot > 0) {
            // 将非 .lrc 的后缀替换为 .lrc（如 .mp3 -> .lrc）
            return filename.substring(0, dot) + ".lrc";
        }
        return filename + ".lrc";
    }

    // 按名称解析歌词路径：先精确再模糊（包含/前缀匹配），找不到返回 null
    private Path resolveLyricsPath(String lrcName) throws IOException {
        Path exact = Paths.get(lrcUploadDir, lrcName);
        if (Files.exists(exact)) return exact;

        // 去掉后缀，做模糊匹配，兼容上传时歌词名与音乐名不一致（如带时间戳）
        final String base;
        int dot = lrcName.lastIndexOf('.');
        if (dot > 0) {
            base = lrcName.substring(0, dot);
        } else {
            base = lrcName;
        }

        // 进一步处理：去掉尾部的 _数字 时间戳、统一比较键
        final String baseNormalized = base.replaceFirst("_[0-9]{6,}$", "");
        final String baseKey = simplifyName(baseNormalized);
        final HashSet<String> baseTokens = new HashSet<>(Arrays.asList(splitTitleTokens(baseNormalized)));

        Path dir = Paths.get(lrcUploadDir);
        if (!Files.exists(dir)) return null;

        return Files.list(dir)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lrc"))
                .filter(p -> {
                    String n = p.getFileName().toString();
                    String nBase = n.substring(0, n.length() - 4); // 去掉 .lrc
                    String nNormalized = nBase.replaceFirst("_[0-9]{6,}$", "");
                    String nKey = simplifyName(nNormalized);

                    // 规则1：直接前缀或包含
                    if (nBase.startsWith(base) || nBase.contains(base) || nKey.contains(baseKey) || baseKey.contains(nKey)) {
                        return true;
                    }

                    // 规则2：标题片段可交换匹配（A - B 与 B - A）
                    HashSet<String> nTokens = new HashSet<>(Arrays.asList(splitTitleTokens(nNormalized)));
                    // 至少有两个以上相同片段，认为匹配
                    int same = 0;
                    for (String t : baseTokens) if (nTokens.contains(t)) same++;
                    return same >= Math.min(2, Math.max(baseTokens.size(), nTokens.size()) - 1);
                })
                .findFirst()
                .orElse(null);
    }

    // 生成宽松匹配用的 key：移除空白、常见符号，仅保留中英文与数字，转小写
    private String simplifyName(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || (c >= '\u4e00' && c <= '\u9fa5')) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    // 将标题按常见分隔符拆分为片段（去除空串并做简化）
    private String[] splitTitleTokens(String s) {
        if (s == null) return new String[0];
        String[] parts = s.split("[\\s\\-—_~·|：:（）\\u3000]+");
        return Arrays.stream(parts)
                .map(this::simplifyName)
                .filter(str -> !str.isEmpty())
                .toArray(String[]::new);
    }

    private String sanitizeForContentDisposition(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "file";
        }
        return filename.replaceAll("[\\r\\n]", " ");
    }

    private MediaType resolveMediaTypeByExtension(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".mp3")) return MediaType.parseMediaType("audio/mpeg");
        if (lower.endsWith(".wav")) return MediaType.parseMediaType("audio/wav");
        if (lower.endsWith(".flac")) return MediaType.parseMediaType("audio/flac");
        if (lower.endsWith(".mp4")) return MediaType.parseMediaType("video/mp4");
        if (lower.endsWith(".mkv")) return MediaType.parseMediaType("video/x-matroska");
        if (lower.endsWith(".avi")) return MediaType.parseMediaType("video/x-msvideo");
        if (lower.endsWith(".lrc") || lower.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    // 删除文件：图片/视频/音乐
    @DeleteMapping("/delete/images/{filename}")
    public ResponseEntity<String> deleteImage(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return deleteFileInternal(imagesUploadDir, decodedFilename);
    }

    @DeleteMapping("/delete/video/{filename}")
    public ResponseEntity<String> deleteVideo(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        return deleteFileInternal(videoUploadDir, decodedFilename);
    }

    @DeleteMapping("/delete/music/{filename}")
    public ResponseEntity<String> deleteMusic(@PathVariable String filename) throws IOException {
        // URL 解码文件名
        String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
        // 同时尝试删除对应的歌词
        String base = decodedFilename;
        int idx = decodedFilename.lastIndexOf('.');
        if (idx > 0) base = decodedFilename.substring(0, idx);
        // 删除音乐文件
        ResponseEntity<String> res = deleteFileInternal(musicUploadDir, decodedFilename);
        // 删除同名 .lrc（存在则删）
        Path lrc = Paths.get(lrcUploadDir, base + ".lrc");
        if (Files.exists(lrc)) {
            try { Files.deleteIfExists(lrc); } catch (Exception ignored) {}
        }
        return res;
    }

    private ResponseEntity<String> deleteFileInternal(String baseDir, String filename) throws IOException {
        // 优先通过映射文件查找内部文件名
        String internalFilename = resolveInternalFilenameFromMapping(baseDir, filename);

        // 如果映射未找到，尝试从数据库查找
        if (internalFilename == null) {
            try {
                // 尝试通过原始文件名查找文件记录
                // 这里我们需要遍历所有文件记录，找到originalFilename等于filename的记录
                // 由于没有直接的mapper方法，我们先获取所有记录，然后在内存中过滤
                List<FileRecord> records = fileRecordService.findAll();
                for (FileRecord record : records) {
                    if (filename.equals(record.getOriginalFilename())) {
                        internalFilename = record.getStoredFilename();
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("从数据库查找文件记录失败: {}", e.getMessage());
            }
        }

        // 如果仍然未找到，则尝试直接使用传入文件名
        if (internalFilename == null) {
            internalFilename = filename;
        }

        Path path = Paths.get(baseDir, internalFilename);
        if (!Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
        }
        Files.delete(path);
        // 同时删除映射文件（如果存在）
        Path map = Paths.get(baseDir, internalFilename + ".map");
        if (Files.exists(map)) {
            try { Files.deleteIfExists(map); } catch (Exception ignored) {}
        }
        return ResponseEntity.ok("File deleted successfully.");
    }
}



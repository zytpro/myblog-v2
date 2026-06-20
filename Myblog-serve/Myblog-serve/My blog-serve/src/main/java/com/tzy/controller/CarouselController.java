package com.tzy.controller;

import com.tzy.pojo.Carousel;
import com.tzy.service.CarouselService;
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
import java.util.List;

@RestController
@RequestMapping("/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @Value("${file.upload.path.carousel}")
    private String CAROUSEL_UPLOAD_DIR;

    // 获取所有轮播图
    @GetMapping
    public List<Carousel> getAllCarousels() {
        return carouselService.getAllCarousels();
    }

    // 获取指定 ID 的轮播图
    @GetMapping("/{id}")
    public Carousel getCarouselById(@PathVariable Integer id) {
        return carouselService.getCarouselById(id);
    }

    // 添加新的轮播图
    @PostMapping
    public ResponseEntity<?> addCarousel(@RequestParam("title") String title,
                                         @RequestParam("status") Integer status,
                                         @RequestParam("imageURL") String imageUrl, // 修改为接收 imageURL
                                         @RequestParam(value = "articleId", required = false) Integer articleId) {
        try {
            // 创建并保存 Carousel 对象
            Carousel carousel = new Carousel();
            carousel.setTitle(title);
            carousel.setStatus(status);
            carousel.setArticleId(articleId);
            carousel.setImageURL(imageUrl); // 使用传递过来的 imageURL

            Carousel savedCarousel = carouselService.addCarousel(carousel);
            return ResponseEntity.ok(savedCarousel);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("轮播图添加失败: " + e.getMessage());
        }
    }


    // 更新轮播图
    @PutMapping("/{id}")
    public Carousel updateCarousel(@PathVariable Integer id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        return carouselService.updateCarousel(carousel);
    }

    // 删除轮播图
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCarousel(@PathVariable Integer id) {
        try {
            // 先获取要删除的轮播图对象
            Carousel carousel = carouselService.getCarouselById(id);
            if (carousel == null) {
                return ResponseEntity.status(404).body("轮播图不存在");
            }

            // 获取文件路径
            String imageUrl = carousel.getImageURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // 从 URL 中提取文件名
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                // 获取文件路径
                Path filePath = Paths.get(CAROUSEL_UPLOAD_DIR, filename);

                // 删除文件
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("文件已删除: " + filePath);
                } else {
                    System.out.println("文件不存在: " + filePath);
                }
            }

            // 删除轮播图记录
            carouselService.deleteCarousel(id);
            return ResponseEntity.ok("轮播图及其图片已删除");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("删除失败: " + e.getMessage());
        }
    }


    // 图片上传接口
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 校验文件类型
            if (!isValidImageType(file)) {
                return ResponseEntity.status(400).body("只允许上传图片文件(jpg, jpeg, png, gif)");
            }

            // 计算文件哈希值并加上扩展名
            String fileHash = FileUtils.calculateFileHash(file);
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = fileHash + extension;

            // 确保目录存在
            Path dirPath = Paths.get(CAROUSEL_UPLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 创建文件路径
            Path filePath = Paths.get(CAROUSEL_UPLOAD_DIR, uniqueFilename);

            // 验证文件是否已存在
            if (Files.exists(filePath)) {
                return ResponseEntity.status(400).body("文件已存在！");
            }

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 生成文件访问 URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            String imageUrl = baseUrl + "/Carousel/" + uniqueFilename;

            return ResponseEntity.ok(imageUrl);

        } catch (IOException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(500).body("文件上传失败: " + e.getMessage());
        }
    }

    // 校验图片文件类型
    private boolean isValidImageType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            return extension.equals(".jpg") || extension.equals(".jpeg")
                || extension.equals(".png") || extension.equals(".gif");
        }
        return false;
    }
}

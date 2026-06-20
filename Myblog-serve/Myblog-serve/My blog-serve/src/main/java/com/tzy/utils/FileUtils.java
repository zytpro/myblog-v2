package com.tzy.utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static String calculateFileHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        //创建 MessageDigest 实例
        MessageDigest digest = MessageDigest.getInstance("SHA-256");  // 使用 SHA-256 算法
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        //读取文件并更新哈希值
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        //生成最终的哈希值
        byte[] hashBytes = digest.digest();
        //转换为十六进制字符串
        /**
         * String.format("%02x", b)：将字节 b 转换为两位的十六进制表示，
         * 并拼接到 hexString 中。
         * %02x 表示每个字节转换为两位的十六进制数，如果需要可以在前面补零。
         */
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        // 返回哈希值 + 文件扩展名
        return hexString.toString();
    }
}

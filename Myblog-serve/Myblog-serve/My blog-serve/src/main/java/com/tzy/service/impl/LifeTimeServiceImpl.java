package com.tzy.service.impl;

import com.tzy.pojo.LifeTime;
import com.tzy.mapper.LifeTimeMapper;
import com.tzy.service.FileUploadService;
import com.tzy.service.LifeTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 生活时光服务实现类
 * 
 * @author tzy
 * @date 2024/04/05
 */
@Service
public class LifeTimeServiceImpl implements LifeTimeService {

    @Autowired
    private LifeTimeMapper lifeTimeMapper;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private HttpServletRequest request;

    @Value("${file.upload.path.lifetime}")
    private String LIFE_TIME_UPLOAD_DIR;

    @Override
    public boolean addLifeTime(MultipartFile file, String content) throws Exception {
        // 上传图片
        FileUploadService.UploadResult result = fileUploadService.uploadFile(file, "lifetime", null, request);
        
        // 保存记录
        LifeTime lifeTime = new LifeTime();
        lifeTime.setContent(content);
        lifeTime.setUrl(result.getUrl());
        
        return lifeTimeMapper.insert(lifeTime) > 0;
    }

    @Override
    public List<LifeTime> getAllLifeTime() {
        return lifeTimeMapper.selectAll();
    }

    @Override
    public boolean deleteLifeTime(String url) {
        // 1. 从URL中提取文件名
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        
        // 2. 构建文件完整路径
        Path filePath = Paths.get(LIFE_TIME_UPLOAD_DIR, fileName);
        File file = filePath.toFile();
        
        // 3. 如果文件存在，则删除
        if (file.exists() && file.isFile()) {
            boolean fileDeleted = file.delete();
            if (!fileDeleted) {
                throw new RuntimeException("文件删除失败");
            }
        }
        
        // 4. 删除数据库记录
        return lifeTimeMapper.deleteByUrl(url) > 0;
    }

    @Override
    public boolean updateLifeTime(LifeTime lifeTime) {
        return lifeTimeMapper.updateByUrl(lifeTime) > 0;
    }
} 
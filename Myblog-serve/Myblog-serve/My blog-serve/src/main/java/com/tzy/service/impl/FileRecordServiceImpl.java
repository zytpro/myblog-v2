package com.tzy.service.impl;

import com.tzy.mapper.FileRecordMapper;
import com.tzy.pojo.FileRecord;
import com.tzy.service.FileRecordService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件记录服务实现类
 */
@Service
public class FileRecordServiceImpl implements FileRecordService {

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Override
    public boolean saveFileRecord(FileRecord fileRecord) {
        return fileRecordMapper.insertFileRecord(fileRecord) > 0;
    }

    @Override
    public FileRecord saveFileRecord(MultipartFile file, String type, String storedFilename, String filePath, String fileHash) {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setOriginalFilename(file.getOriginalFilename());
        fileRecord.setStoredFilename(storedFilename);
        fileRecord.setFileHash(fileHash);
        fileRecord.setFileType(type);
        fileRecord.setFileSize(file.getSize());
        fileRecord.setUploadTime(LocalDateTime.now());
        fileRecord.setFilePath(filePath);
        fileRecord.setMimeType(file.getContentType());
        fileRecord.setDownloadCount(0);
        
        fileRecordMapper.insertFileRecord(fileRecord);
        return fileRecord;
    }

    @Override
    public FileRecord findByFileHash(String fileHash) {
        return fileRecordMapper.findByFileHash(fileHash);
    }

    @Override
    public FileRecord findByStoredFilename(String storedFilename) {
        return fileRecordMapper.findByStoredFilename(storedFilename);
    }

    @Override
    public List<FileRecord> findByFileType(String fileType) {
        return fileRecordMapper.findByFileType(fileType);
    }

    @Override
    public List<FileRecord> findAll() {
        return fileRecordMapper.findAll();
    }

    @Override
    public FileRecord getById(Long id) {
        return fileRecordMapper.getById(id);
    }

    @Override
    public void incrementDownloadCount(Long id) {
        fileRecordMapper.incrementDownloadCount(id);
    }

    @Override
    public void deleteById(Long id) {
        fileRecordMapper.deleteById(id);
    }

    @Override
    public FileRecord save(FileRecord fileRecord) {
        if (fileRecord.getId() == null) {
            fileRecordMapper.insertFileRecord(fileRecord);
        } else {
            fileRecordMapper.saveOrUpdate(fileRecord);
        }
        return fileRecord;
    }
}
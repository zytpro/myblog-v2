package com.tzy.service;

import com.tzy.pojo.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件记录服务接口
 */
public interface FileRecordService {
    /**
     * 保存文件记录
     * @param fileRecord 文件记录
     * @return 保存结果
     */
    boolean saveFileRecord(FileRecord fileRecord);

    /**
     * 保存文件记录（多参数版本）
     * @param file 上传的文件
     * @param type 文件类型
     * @param storedFilename 存储的文件名
     * @param filePath 文件路径
     * @param fileHash 文件哈希值
     * @return 文件记录
     */
    FileRecord saveFileRecord(MultipartFile file, String type, String storedFilename, String filePath, String fileHash);

    /**
     * 根据文件哈希值查找文件记录
     * @param fileHash 文件哈希值
     * @return 文件记录
     */
    FileRecord findByFileHash(String fileHash);

    /**
     * 根据存储的文件名查找文件记录
     * @param storedFilename 存储的文件名
     * @return 文件记录
     */
    FileRecord findByStoredFilename(String storedFilename);

    /**
     * 根据文件类型查找文件记录
     * @param fileType 文件类型
     * @return 文件记录列表
     */
    List<FileRecord> findByFileType(String fileType);

    /**
     * 查找所有文件记录
     * @return 文件记录列表
     */
    List<FileRecord> findAll();

    /**
     * 根据ID查找文件记录
     * @param id 文件ID
     * @return 文件记录
     */
    FileRecord getById(Long id);

    /**
     * 增加文件下载次数
     * @param id 文件ID
     */
    void incrementDownloadCount(Long id);

    /**
     * 根据ID删除文件记录
     * @param id 文件ID
     */
    void deleteById(Long id);

    /**
     * 保存文件记录（通用方法）
     * @param fileRecord 文件记录
     * @return 文件记录
     */
    FileRecord save(FileRecord fileRecord);
}
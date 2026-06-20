package com.tzy.mapper;

import com.tzy.pojo.FileRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文件记录Mapper
 */
public interface FileRecordMapper {
    @Insert("INSERT INTO file_records (original_filename, stored_filename, file_hash, file_type, file_size, file_path, mime_type, download_count) VALUES (#{originalFilename}, #{storedFilename}, #{fileHash}, #{fileType}, #{fileSize}, #{filePath}, #{mimeType}, #{downloadCount})")
    int insertFileRecord(FileRecord fileRecord);

    @Select("SELECT * FROM file_records WHERE file_hash = #{fileHash}")
    FileRecord findByFileHash(String fileHash);

    @Select("SELECT * FROM file_records WHERE stored_filename = #{storedFilename}")
    FileRecord findByStoredFilename(String storedFilename);

    @Select("SELECT * FROM file_records WHERE file_type = #{fileType} ORDER BY upload_time DESC")
    List<FileRecord> findByFileType(String fileType);

    @Select("SELECT * FROM file_records ORDER BY upload_time DESC")
    List<FileRecord> findAll();

    @Select("SELECT * FROM file_records WHERE id = #{id}")
    FileRecord getById(Long id);

    @Update("UPDATE file_records SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(Long id);

    @Update("DELETE FROM file_records WHERE id = #{id}")
    void deleteById(Long id);

    @Update("INSERT INTO file_records (original_filename, stored_filename, file_hash, file_type, file_size, file_path, mime_type, download_count) VALUES (#{originalFilename}, #{storedFilename}, #{fileHash}, #{fileType}, #{fileSize}, #{filePath}, #{mimeType}, #{downloadCount}) ON DUPLICATE KEY UPDATE original_filename = #{originalFilename}, stored_filename = #{storedFilename}, file_type = #{fileType}, file_size = #{fileSize}, file_path = #{filePath}, mime_type = #{mimeType}, download_count = #{downloadCount}")
    int saveOrUpdate(FileRecord fileRecord);
}
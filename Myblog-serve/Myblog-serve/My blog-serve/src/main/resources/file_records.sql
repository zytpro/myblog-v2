-- 文件记录表
CREATE TABLE IF NOT EXISTS `file_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_filename` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_filename` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_hash` varchar(64) NOT NULL COMMENT '文件哈希值',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `download_count` int(11) DEFAULT 0 COMMENT '下载次数',
  `description` text COMMENT '文件描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_hash` (`file_hash`),
  KEY `idx_original_filename` (`original_filename`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';


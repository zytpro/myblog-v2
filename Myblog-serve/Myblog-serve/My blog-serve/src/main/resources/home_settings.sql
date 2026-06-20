CREATE TABLE IF NOT EXISTS `home_settings` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `background_image` VARCHAR(255) DEFAULT '/img/back1.png' COMMENT '背景图片路径',
  `typewriter_texts` TEXT COMMENT '打字机文本，逗号分隔',
  `web_title` VARCHAR(100) DEFAULT '我的博客' COMMENT '网站标题',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页设置表';

-- 插入默认数据
INSERT INTO `home_settings` (`background_image`, `typewriter_texts`, `web_title`) 
VALUES 
('/img/back1.png', '你看对面的青山多漂亮,春风得意马蹄疾，一日看尽长安花,山重水复疑无路，柳暗花明又一村,会当凌绝顶，一览众山小,海上生明月，天涯共此时,落红不是无情物，化作春泥更护花,长风破浪会有时，直挂云帆济沧海,天生我材必有用，千金散尽还复来', '我的博客')
ON DUPLICATE KEY UPDATE 
`background_image` = VALUES(`background_image`),
`typewriter_texts` = VALUES(`typewriter_texts`),
`web_title` = VALUES(`web_title`);

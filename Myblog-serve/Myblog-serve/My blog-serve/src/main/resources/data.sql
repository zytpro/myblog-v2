-- 初始化用户数据
-- 默认管理员用户：admin，密码：admin123（MD5加密后）
INSERT INTO users (username, password, avatar, email, rule, phone, description) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '/icon/001.png', 'admin@example.com', '0', '13800138000', '系统管理员')
ON DUPLICATE KEY UPDATE username = username;

-- 默认普通用户：user，密码：user123（MD5加密后）
INSERT INTO users (username, password, avatar, email, rule, phone, description) 
VALUES ('user', '6ad14ba9986e3615423dfca256d04e3f', '/icon/001.png', 'user@example.com', '1', '13800138001', '普通用户')
ON DUPLICATE KEY UPDATE username = username;


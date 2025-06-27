use relics;

-- ----------------------------
  KEY `idx_status` (`status`)
     `phone_number` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号码',
     `avatar_url` VARCHAR(256) NULL DEFAULT NULL COMMENT '头像URL',

ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);
     `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '用户状态 (例如: 1=正常, 0=禁用)',
  KEY `idx_username` (`username`),
  UNIQUE KEY `uk_username` (`username`),
  PRIMARY KEY (`user_id`),
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN、USER',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(100) NOT NULL COMMENT '密码（加密后）',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
CREATE TABLE IF NOT EXISTS `users` (
-- 创建用户表

CREATE TABLE `users` (
     `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     `user_id` VARCHAR(32) NOT NULL COMMENT '用户唯一标识',
     `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
     `full_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '真实姓名',
     `password` VARCHAR(256) NOT NULL COMMENT '密码 (应加密存储)',
     `email` VARCHAR(32) NULL DEFAULT NULL COMMENT '邮箱',
--  Table structure for `users` (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `users`;
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
-- 插入默认管理员用户（密码为：admin123，已使用BCrypt加密）
     `role` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '角色 (例如: user, expert)',
-- 插入测试普通用户（密码为：user123，已使用BCrypt加密）
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);
VALUES ('user001', 'user', '$2a$10$7qHnUcL.8KpY9Lh5HQPv6uJ8LfV2WgR.9dGc7VK0.3HyQ5M4f1aFy', 'user@example.com', 'USER', 1)
     `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '头衔/职位',
     `permission` INT NULL DEFAULT 0 COMMENT '权限级别或位掩码',
     `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_user_id` (`user_id`),
     UNIQUE KEY `uk_email` (`email`),
     UNIQUE KEY `uk_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);
VALUES ('user001', 'user', '$2a$10$7qHnUcL.8KpY9Lh5HQPv6uJ8LfV2WgR.9dGc7VK0.3HyQ5M4f1aFy', 'user@example.com', 'USER', 1)
INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `role`, `status`)
-- 插入测试普通用户（密码为：user123，已使用BCrypt加密）

ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);
VALUES ('admin001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRJoYvO3KS0EGaYQF1N1nOOJlr2zTG4LFDqGGy', 'admin@example.com', 'ADMIN', 1)
INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `role`, `status`)
-- 插入默认管理员用户（密码为：admin123，已使用BCrypt加密）

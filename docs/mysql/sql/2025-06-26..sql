create database if not exists relics default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use relics;

-- ----------------------------
--  Table structure for `users` (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
     `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     `username` VARCHAR(32) NOT NULL COMMENT '用户唯一标识',
     `nickname` VARCHAR(64) NULL COMMENT '昵称',
     `full_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '真实姓名',
     `password` VARCHAR(256) NOT NULL COMMENT '密码 (应加密存储)',
     `email` VARCHAR(32) NULL DEFAULT NULL COMMENT '邮箱',
     `phone_number` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号码',
     `avatar_url` VARCHAR(256) NULL DEFAULT NULL COMMENT '头像URL',
     `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '用户状态 (例如: 1=正常, 0=禁用)',
     `role` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '角色 (例如: user, expert)',
     `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '头衔/职位',
     `permission` INT NULL DEFAULT 0 COMMENT '权限级别或位掩码',
     `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_user_name` (`username`),
     UNIQUE KEY `uk_email` (`email`),
     UNIQUE KEY `uk_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


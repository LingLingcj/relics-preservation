-- 创建文物评论表
CREATE TABLE `relics_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `relics_id` bigint NOT NULL COMMENT '文物ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_relics_id` (`relics_id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物评论表';

-- 添加外键约束（可选，根据实际需求决定是否添加）
-- ALTER TABLE `relics_comment` ADD CONSTRAINT `fk_relics_comment_relics` FOREIGN KEY (`relics_id`) REFERENCES `relics` (`id`);
-- ALTER TABLE `relics_comment` ADD CONSTRAINT `fk_relics_comment_user` FOREIGN KEY (`username`) REFERENCES `user` (`username`);

-- 创建文物收藏表
CREATE TABLE IF NOT EXISTS `favorites` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `relics_id` BIGINT NOT NULL COMMENT '文物ID',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_username` (`username`),
    INDEX `idx_relics_id` (`relics_id`),
    UNIQUE INDEX `uk_username_relics` (`username`, `relics_id`) COMMENT '确保用户对同一文物只能收藏一次',
    CONSTRAINT `fk_favorites_username` FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物收藏表';

-- 添加告警记录表
CREATE TABLE IF NOT EXISTS `alert_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_id` varchar(64) NOT NULL COMMENT '告警ID',
  `sensor_id` varchar(64) NOT NULL COMMENT '传感器ID',
  `alert_type` varchar(32) NOT NULL COMMENT '告警类型',
  `severity` varchar(16) NOT NULL COMMENT '告警级别：INFO、WARNING、CRITICAL',
  `message` varchar(255) NOT NULL COMMENT '告警消息',
  `relics_id` bigint(20) DEFAULT NULL COMMENT '文物ID',
  `location_id` bigint(20) DEFAULT NULL COMMENT '位置ID',
  `current_value` double DEFAULT NULL COMMENT '当前读数',
  `threshold` double DEFAULT NULL COMMENT '阈值',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '告警状态：ACTIVE、RESOLVED',
  `timestamp` datetime NOT NULL COMMENT '告警时间',
  `resolved_time` datetime DEFAULT NULL COMMENT '解决时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_id` (`alert_id`),
  KEY `idx_sensor_id` (`sensor_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_status` (`status`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表'; 
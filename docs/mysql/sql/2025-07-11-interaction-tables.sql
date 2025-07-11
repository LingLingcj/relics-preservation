-- =====================================================
-- 用户交互领域数据库表结构
-- 创建时间: 2025-07-11
-- 描述: 替换原有的 favorite 和 comment 表，统一管理用户与文物的交互
-- =====================================================

-- 用户收藏表（新）
CREATE TABLE `user_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `relics_id` bigint NOT NULL COMMENT '文物ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username_relics` (`username`, `relics_id`),
  KEY `idx_username` (`username`),
  KEY `idx_relics_id` (`relics_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 用户评论表（新）
CREATE TABLE `user_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `comment_id` bigint NOT NULL COMMENT '评论业务ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `relics_id` bigint NOT NULL COMMENT '文物ID',
  `content` text NOT NULL COMMENT '评论内容',
  `comment_status` tinyint NOT NULL DEFAULT '0' COMMENT '评论状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_id` (`comment_id`),
  KEY `idx_username` (`username`),
  KEY `idx_relics_id` (`relics_id`),
  KEY `idx_comment_status` (`comment_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评论表';

-- 交互统计表（可选，用于缓存统计数据）
CREATE TABLE `interaction_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `relics_id` bigint NOT NULL COMMENT '文物ID',
  `favorite_count` bigint NOT NULL DEFAULT '0' COMMENT '收藏数量',
  `comment_count` bigint NOT NULL DEFAULT '0' COMMENT '评论数量',
  `total_interactions` bigint NOT NULL DEFAULT '0' COMMENT '总交互次数',
  `last_interaction_time` datetime DEFAULT NULL COMMENT '最后交互时间',
  `popularity_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '热度分数',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relics_id` (`relics_id`),
  KEY `idx_favorite_count` (`favorite_count`),
  KEY `idx_comment_count` (`comment_count`),
  KEY `idx_popularity_score` (`popularity_score`),
  KEY `idx_last_interaction_time` (`last_interaction_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交互统计表';

-- 用户活动记录表（可选，用于记录用户行为轨迹）
CREATE TABLE `user_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `relics_id` bigint DEFAULT NULL COMMENT '文物ID',
  `activity_type` varchar(20) NOT NULL COMMENT '活动类型：FAVORITE_ADDED, FAVORITE_REMOVED, COMMENT_ADDED, COMMENT_DELETED',
  `activity_description` varchar(200) DEFAULT NULL COMMENT '活动描述',
  `activity_data` json DEFAULT NULL COMMENT '活动数据（JSON格式）',
  `activity_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活动时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_relics_id` (`relics_id`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_activity_time` (`activity_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动记录表';

-- 评论审核日志表
CREATE TABLE `comment_review_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `comment_id` bigint NOT NULL COMMENT '评论业务ID',
  `reviewer` varchar(50) NOT NULL COMMENT '审核人',
  `action` varchar(20) NOT NULL COMMENT '审核操作：APPROVE, REJECT',
  `reason` varchar(500) DEFAULT NULL COMMENT '审核理由',
  `before_status` tinyint NOT NULL COMMENT '审核前状态',
  `after_status` tinyint NOT NULL COMMENT '审核后状态',
  `review_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `comment_author` varchar(50) DEFAULT NULL COMMENT '评论作者',
  `relics_id` bigint DEFAULT NULL COMMENT '文物ID',
  `comment_content` text DEFAULT NULL COMMENT '评论内容快照',
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_reviewer` (`reviewer`),
  KEY `idx_action` (`action`),
  KEY `idx_review_time` (`review_time`),
  KEY `idx_comment_author` (`comment_author`),
  KEY `idx_relics_id` (`relics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论审核日志表';

-- 数据迁移脚本：从旧表迁移到新表
-- 注意：执行前请备份原有数据

-- 迁移收藏数据（如果存在旧的收藏表）
INSERT INTO `user_favorites` (`username`, `relics_id`, `create_time`, `update_time`, `status`)
SELECT 
    `username`,
    `relics_id`,
    COALESCE(`create_time`, NOW()),
    COALESCE(`update_time`, NOW()),
    CASE WHEN `status` = 0 THEN 0 ELSE 1 END
FROM `relics_favorite` 
WHERE `relics_id` IS NOT NULL AND `username` IS NOT NULL
ON DUPLICATE KEY UPDATE 
    `update_time` = VALUES(`update_time`),
    `status` = VALUES(`status`);

-- 迁移评论数据
INSERT INTO `user_comments` (`comment_id`, `username`, `relics_id`, `content`, `comment_status`, `create_time`, `update_time`, `status`)
SELECT 
    `id` as `comment_id`,
    `username`,
    `relics_id`,
    `content`,
    CASE WHEN `status` = 0 THEN 1 ELSE 2 END as `comment_status`, -- 假设原来status=0表示正常，转换为已通过
    COALESCE(`create_time`, NOW()),
    COALESCE(`update_time`, NOW()),
    CASE WHEN `status` = 0 THEN 0 ELSE 1 END
FROM `relics_comment` 
WHERE `relics_id` IS NOT NULL AND `username` IS NOT NULL AND `content` IS NOT NULL
ON DUPLICATE KEY UPDATE 
    `content` = VALUES(`content`),
    `comment_status` = VALUES(`comment_status`),
    `update_time` = VALUES(`update_time`),
    `status` = VALUES(`status`);

-- 初始化交互统计数据
INSERT INTO `interaction_statistics` (`relics_id`, `favorite_count`, `comment_count`, `total_interactions`, `last_interaction_time`, `popularity_score`)
SELECT 
    r.`relics_id`,
    COALESCE(f.`favorite_count`, 0) as `favorite_count`,
    COALESCE(c.`comment_count`, 0) as `comment_count`,
    COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) as `total_interactions`,
    GREATEST(COALESCE(f.`last_favorite_time`, '1970-01-01'), COALESCE(c.`last_comment_time`, '1970-01-01')) as `last_interaction_time`,
    (COALESCE(f.`favorite_count`, 0) * 2.0 + COALESCE(c.`comment_count`, 0) * 1.0) as `popularity_score`
FROM (
    SELECT DISTINCT `relics_id` FROM `user_favorites` WHERE `status` = 0
    UNION
    SELECT DISTINCT `relics_id` FROM `user_comments` WHERE `status` = 0
) r
LEFT JOIN (
    SELECT 
        `relics_id`,
        COUNT(*) as `favorite_count`,
        MAX(`create_time`) as `last_favorite_time`
    FROM `user_favorites` 
    WHERE `status` = 0 
    GROUP BY `relics_id`
) f ON r.`relics_id` = f.`relics_id`
LEFT JOIN (
    SELECT 
        `relics_id`,
        COUNT(*) as `comment_count`,
        MAX(`create_time`) as `last_comment_time`
    FROM `user_comments` 
    WHERE `status` = 0 
    GROUP BY `relics_id`
) c ON r.`relics_id` = c.`relics_id`
ON DUPLICATE KEY UPDATE 
    `favorite_count` = VALUES(`favorite_count`),
    `comment_count` = VALUES(`comment_count`),
    `total_interactions` = VALUES(`total_interactions`),
    `last_interaction_time` = VALUES(`last_interaction_time`),
    `popularity_score` = VALUES(`popularity_score`);

-- 创建视图以便于查询（可选）
CREATE OR REPLACE VIEW `v_relics_interaction_summary` AS
SELECT 
    r.`relics_id`,
    r.`name` as `relics_name`,
    COALESCE(s.`favorite_count`, 0) as `favorite_count`,
    COALESCE(s.`comment_count`, 0) as `comment_count`,
    COALESCE(s.`total_interactions`, 0) as `total_interactions`,
    s.`last_interaction_time`,
    COALESCE(s.`popularity_score`, 0) as `popularity_score`
FROM `relics` r
LEFT JOIN `interaction_statistics` s ON r.`relics_id` = s.`relics_id`
WHERE r.`status` = 1; -- 假设status=1表示正常状态

-- 创建用户交互摘要视图
CREATE OR REPLACE VIEW `v_user_interaction_summary` AS
SELECT 
    u.`username`,
    COALESCE(f.`favorite_count`, 0) as `favorite_count`,
    COALESCE(c.`comment_count`, 0) as `comment_count`,
    COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) as `total_interactions`,
    GREATEST(COALESCE(f.`last_favorite_time`, '1970-01-01'), COALESCE(c.`last_comment_time`, '1970-01-01')) as `last_active_time`,
    CASE 
        WHEN COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) = 0 THEN 'INACTIVE'
        WHEN COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) < 10 THEN 'LOW'
        WHEN COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) < 50 THEN 'MEDIUM'
        WHEN COALESCE(f.`favorite_count`, 0) + COALESCE(c.`comment_count`, 0) < 200 THEN 'HIGH'
        ELSE 'VERY_HIGH'
    END as `activity_level`
FROM `users` u
LEFT JOIN (
    SELECT 
        `username`,
        COUNT(*) as `favorite_count`,
        MAX(`create_time`) as `last_favorite_time`
    FROM `user_favorites` 
    WHERE `status` = 0 
    GROUP BY `username`
) f ON u.`username` = f.`username`
LEFT JOIN (
    SELECT 
        `username`,
        COUNT(*) as `comment_count`,
        MAX(`create_time`) as `last_comment_time`
    FROM `user_comments` 
    WHERE `status` = 0 
    GROUP BY `username`
) c ON u.`username` = c.`username`
WHERE u.`status` = 1; -- 假设status=1表示正常状态

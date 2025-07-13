-- =============================================
-- 收藏馆功能数据库表设计
-- Author: LingRJ
-- Date: 2025-07-13
-- Description: 用户收藏馆功能的数据库表结构
-- =============================================

-- 创建收藏馆表
DROP TABLE IF EXISTS `collection_gallery`;
CREATE TABLE `collection_gallery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `gallery_id` varchar(36) NOT NULL COMMENT '收藏馆唯一标识(UUID)',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `name` varchar(128) NOT NULL COMMENT '收藏馆名称',
  `description` text COMMENT '收藏馆描述',
  `theme` varchar(32) NOT NULL COMMENT '主题代码(bronze,porcelain,painting等)',
  `display_style` varchar(32) NOT NULL COMMENT '展示风格代码(grid,list,timeline等)',
  `relics_ids` text COMMENT '文物ID列表，逗号分隔',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开(0=私有,1=公开)',
  `share_code` varchar(32) DEFAULT NULL COMMENT '分享码',
  `custom_theme_name` varchar(64) DEFAULT NULL COMMENT '自定义主题名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0=正常,1=已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gallery_id` (`gallery_id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_username` (`username`),
  KEY `idx_theme` (`theme`),
  KEY `idx_is_public` (`is_public`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`),
  KEY `idx_username_status` (`username`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏馆表';

-- 创建收藏馆统计表（用于缓存统计数据，提高查询性能）
DROP TABLE IF EXISTS `gallery_statistics`;
CREATE TABLE `gallery_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `total_galleries` int NOT NULL DEFAULT '0' COMMENT '收藏馆总数',
  `public_galleries` int NOT NULL DEFAULT '0' COMMENT '公开收藏馆数',
  `private_galleries` int NOT NULL DEFAULT '0' COMMENT '私有收藏馆数',
  `total_relics` int NOT NULL DEFAULT '0' COMMENT '收藏文物总数',
  `most_used_theme` varchar(32) DEFAULT NULL COMMENT '最常用主题',
  `last_gallery_time` datetime DEFAULT NULL COMMENT '最后创建收藏馆时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_total_galleries` (`total_galleries`),
  KEY `idx_total_relics` (`total_relics`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏馆统计表';

-- 创建收藏馆主题统计表（用于分析热门主题）
DROP TABLE IF EXISTS `gallery_theme_statistics`;
CREATE TABLE `gallery_theme_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `theme` varchar(32) NOT NULL COMMENT '主题代码',
  `theme_name` varchar(64) NOT NULL COMMENT '主题名称',
  `gallery_count` int NOT NULL DEFAULT '0' COMMENT '使用该主题的收藏馆数量',
  `user_count` int NOT NULL DEFAULT '0' COMMENT '使用该主题的用户数量',
  `total_relics` int NOT NULL DEFAULT '0' COMMENT '该主题下的文物总数',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_theme` (`theme`),
  KEY `idx_gallery_count` (`gallery_count`),
  KEY `idx_user_count` (`user_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏馆主题统计表';

-- 插入初始主题统计数据
INSERT INTO `gallery_theme_statistics` (`theme`, `theme_name`, `gallery_count`, `user_count`, `total_relics`) VALUES
('bronze', '青铜器', 0, 0, 0),
('porcelain', '瓷器', 0, 0, 0),
('painting', '书画', 0, 0, 0),
('jade', '玉器', 0, 0, 0),
('calligraphy', '书法', 0, 0, 0),
('sculpture', '雕塑', 0, 0, 0),
('furniture', '家具', 0, 0, 0),
('textile', '织物', 0, 0, 0),
('coin', '钱币', 0, 0, 0),
('weapon', '兵器', 0, 0, 0),
('ornament', '饰品', 0, 0, 0),
('instrument', '乐器', 0, 0, 0),
('custom', '自定义', 0, 0, 0);

-- 创建视图：收藏馆详情视图（包含统计信息）
CREATE OR REPLACE VIEW `v_gallery_detail` AS
SELECT 
    g.`id`,
    g.`gallery_id`,
    g.`username`,
    g.`name`,
    g.`description`,
    g.`theme`,
    g.`display_style`,
    g.`relics_ids`,
    g.`is_public`,
    g.`share_code`,
    g.`custom_theme_name`,
    g.`create_time`,
    g.`update_time`,
    g.`status`,
    -- 计算文物数量
    CASE 
        WHEN g.`relics_ids` IS NULL OR g.`relics_ids` = '' THEN 0
        ELSE (LENGTH(g.`relics_ids`) - LENGTH(REPLACE(g.`relics_ids`, ',', '')) + 1)
    END AS `relics_count`,
    -- 主题信息
    ts.`theme_name`,
    ts.`gallery_count` AS `theme_gallery_count`
FROM `collection_gallery` g
LEFT JOIN `gallery_theme_statistics` ts ON g.`theme` = ts.`theme`
WHERE g.`status` = 0;

-- 创建视图：用户收藏馆概览
CREATE OR REPLACE VIEW `v_user_gallery_overview` AS
SELECT 
    g.`username`,
    COUNT(*) AS `total_galleries`,
    SUM(CASE WHEN g.`is_public` = 1 THEN 1 ELSE 0 END) AS `public_galleries`,
    SUM(CASE WHEN g.`is_public` = 0 THEN 1 ELSE 0 END) AS `private_galleries`,
    SUM(
        CASE 
            WHEN g.`relics_ids` IS NULL OR g.`relics_ids` = '' THEN 0
            ELSE (LENGTH(g.`relics_ids`) - LENGTH(REPLACE(g.`relics_ids`, ',', '')) + 1)
        END
    ) AS `total_relics`,
    MAX(g.`create_time`) AS `last_gallery_time`,
    -- 最常用主题
    (
        SELECT g2.`theme` 
        FROM `collection_gallery` g2 
        WHERE g2.`username` = g.`username` AND g2.`status` = 0
        GROUP BY g2.`theme` 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    ) AS `most_used_theme`
FROM `collection_gallery` g
WHERE g.`status` = 0
GROUP BY g.`username`;

-- 创建触发器：自动更新用户收藏馆统计
DELIMITER $$

CREATE TRIGGER `tr_gallery_statistics_insert` 
AFTER INSERT ON `collection_gallery`
FOR EACH ROW
BEGIN
    -- 更新用户统计
    INSERT INTO `gallery_statistics` (`username`, `total_galleries`, `public_galleries`, `private_galleries`, `total_relics`, `last_gallery_time`)
    SELECT 
        NEW.`username`,
        COUNT(*),
        SUM(CASE WHEN `is_public` = 1 THEN 1 ELSE 0 END),
        SUM(CASE WHEN `is_public` = 0 THEN 1 ELSE 0 END),
        SUM(
            CASE 
                WHEN `relics_ids` IS NULL OR `relics_ids` = '' THEN 0
                ELSE (LENGTH(`relics_ids`) - LENGTH(REPLACE(`relics_ids`, ',', '')) + 1)
            END
        ),
        MAX(`create_time`)
    FROM `collection_gallery`
    WHERE `username` = NEW.`username` AND `status` = 0
    ON DUPLICATE KEY UPDATE
        `total_galleries` = VALUES(`total_galleries`),
        `public_galleries` = VALUES(`public_galleries`),
        `private_galleries` = VALUES(`private_galleries`),
        `total_relics` = VALUES(`total_relics`),
        `last_gallery_time` = VALUES(`last_gallery_time`);
    
    -- 更新主题统计
    UPDATE `gallery_theme_statistics` 
    SET 
        `gallery_count` = (
            SELECT COUNT(*) 
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        ),
        `user_count` = (
            SELECT COUNT(DISTINCT `username`) 
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        ),
        `total_relics` = (
            SELECT SUM(
                CASE 
                    WHEN `relics_ids` IS NULL OR `relics_ids` = '' THEN 0
                    ELSE (LENGTH(`relics_ids`) - LENGTH(REPLACE(`relics_ids`, ',', '')) + 1)
                END
            )
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        )
    WHERE `theme` = NEW.`theme`;
END$$

CREATE TRIGGER `tr_gallery_statistics_update` 
AFTER UPDATE ON `collection_gallery`
FOR EACH ROW
BEGIN
    -- 更新用户统计
    INSERT INTO `gallery_statistics` (`username`, `total_galleries`, `public_galleries`, `private_galleries`, `total_relics`, `last_gallery_time`)
    SELECT 
        NEW.`username`,
        COUNT(*),
        SUM(CASE WHEN `is_public` = 1 THEN 1 ELSE 0 END),
        SUM(CASE WHEN `is_public` = 0 THEN 1 ELSE 0 END),
        SUM(
            CASE 
                WHEN `relics_ids` IS NULL OR `relics_ids` = '' THEN 0
                ELSE (LENGTH(`relics_ids`) - LENGTH(REPLACE(`relics_ids`, ',', '')) + 1)
            END
        ),
        MAX(`create_time`)
    FROM `collection_gallery`
    WHERE `username` = NEW.`username` AND `status` = 0
    ON DUPLICATE KEY UPDATE
        `total_galleries` = VALUES(`total_galleries`),
        `public_galleries` = VALUES(`public_galleries`),
        `private_galleries` = VALUES(`private_galleries`),
        `total_relics` = VALUES(`total_relics`),
        `last_gallery_time` = VALUES(`last_gallery_time`);
    
    -- 更新新主题统计
    UPDATE `gallery_theme_statistics` 
    SET 
        `gallery_count` = (
            SELECT COUNT(*) 
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        ),
        `user_count` = (
            SELECT COUNT(DISTINCT `username`) 
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        ),
        `total_relics` = (
            SELECT SUM(
                CASE 
                    WHEN `relics_ids` IS NULL OR `relics_ids` = '' THEN 0
                    ELSE (LENGTH(`relics_ids`) - LENGTH(REPLACE(`relics_ids`, ',', '')) + 1)
                END
            )
            FROM `collection_gallery` 
            WHERE `theme` = NEW.`theme` AND `status` = 0
        )
    WHERE `theme` = NEW.`theme`;
    
    -- 如果主题发生变化，也要更新旧主题统计
    IF OLD.`theme` != NEW.`theme` THEN
        UPDATE `gallery_theme_statistics` 
        SET 
            `gallery_count` = (
                SELECT COUNT(*) 
                FROM `collection_gallery` 
                WHERE `theme` = OLD.`theme` AND `status` = 0
            ),
            `user_count` = (
                SELECT COUNT(DISTINCT `username`) 
                FROM `collection_gallery` 
                WHERE `theme` = OLD.`theme` AND `status` = 0
            ),
            `total_relics` = (
                SELECT SUM(
                    CASE 
                        WHEN `relics_ids` IS NULL OR `relics_ids` = '' THEN 0
                        ELSE (LENGTH(`relics_ids`) - LENGTH(REPLACE(`relics_ids`, ',', '')) + 1)
                    END
                )
                FROM `collection_gallery` 
                WHERE `theme` = OLD.`theme` AND `status` = 0
            )
        WHERE `theme` = OLD.`theme`;
    END IF;
END$$

DELIMITER ;

-- 创建索引优化查询性能
CREATE INDEX `idx_gallery_relics_search` ON `collection_gallery` (`username`, `status`, `theme`);
CREATE INDEX `idx_gallery_public_search` ON `collection_gallery` (`is_public`, `status`, `create_time`);
CREATE INDEX `idx_gallery_share_search` ON `collection_gallery` (`share_code`, `is_public`, `status`);

-- 添加表注释
ALTER TABLE `collection_gallery` COMMENT = '用户收藏馆表 - 存储用户创建的个人收藏馆信息';
ALTER TABLE `gallery_statistics` COMMENT = '收藏馆统计表 - 缓存用户收藏馆统计数据，提高查询性能';
ALTER TABLE `gallery_theme_statistics` COMMENT = '收藏馆主题统计表 - 统计各主题的使用情况，用于分析和推荐';

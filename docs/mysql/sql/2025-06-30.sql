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
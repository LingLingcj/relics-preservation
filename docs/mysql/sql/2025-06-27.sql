-- ----------------------------
-- Table structure for location (位置表)
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
   `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
   `name` VARCHAR(128) NOT NULL COMMENT '位置名称 (如: "一号展厅A区3号展柜")',
   `description` TEXT NULL COMMENT '位置详细描述',
   `parent_id` INT NULL DEFAULT NULL COMMENT '父级位置ID，用于构建层级关系，NULL表示顶级位置',
   `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
   PRIMARY KEY (`id`),
   KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='位置表';

-- ----------------------------
-- Table structure for relics (文物表)
-- ----------------------------
DROP TABLE IF EXISTS `relics`;
CREATE TABLE `relics` (
     `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     `relics_id` VARCHAR(64) NOT NULL COMMENT '文物业务ID (如馆藏编号)，全局唯一',
     `name` VARCHAR(128) NOT NULL COMMENT '文物名称',
     `description` TEXT NULL COMMENT '文物详细描述',
     `preservation` TINYINT NOT NULL COMMENT '保护等级 (如: 1=一级, 2=二级, 3=珍贵)',
     `category` VARCHAR(64) NULL DEFAULT NULL COMMENT '类别 (如: 青铜器, 瓷器, 书画)',
     `era` VARCHAR(32) NULL DEFAULT NULL COMMENT '所属年代 (如: 商代, 明朝)',
     `material` VARCHAR(64) NULL DEFAULT NULL COMMENT '主要材质 (如: 青铜, 陶瓷)',
     `image_url` VARCHAR(256) NULL DEFAULT NULL COMMENT '文物图片链接',
     `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 (0: 库房存储, 1: 展出中, 2: 修复中)',
     `location_id` INT NULL DEFAULT NULL COMMENT '所在位置ID，关联location表',
     `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_relics_id` (`relics_id`),
     KEY `idx_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文物表';

-- ----------------------------
-- Table structure for sensor (传感器信息表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor`;
CREATE TABLE `sensor` (
     `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     `sensor_id` VARCHAR(64) NOT NULL COMMENT '传感器唯一标识 (如MAC地址或序列号)',
     `name` VARCHAR(128) NOT NULL COMMENT '设备名称 (方便识别, 如: "A展厅3号柜-温湿度计")',
     `type` VARCHAR(32) NOT NULL COMMENT '传感器类型 (如: temperature, humidity, light, vibration)',
     `model` VARCHAR(64) NULL DEFAULT NULL COMMENT '设备型号',
     `location_id` INT NOT NULL COMMENT '安装位置ID，关联location表',
     `relic_id` INT NULL DEFAULT NULL COMMENT '关联的文物ID，如果传感器是针对特定文物，关联relics表',
     `status` TINYINT NOT NULL DEFAULT 1 COMMENT '设备状态 (0: 离线, 1: 在线, 2: 故障)',
     `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_sensor_id` (`sensor_id`),
     KEY `idx_location_id` (`location_id`),
     KEY `idx_relic_id` (`relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器信息表';
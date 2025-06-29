-- ----------------------------
-- Table structure for sensor_data (传感器原始数据表)
-- ----------------------------

DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE sensor_data (
     id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
     sensor_id VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
     type VARCHAR(32) NOT NULL COMMENT '传感器类型 (temperature, humidity, light等)',
     value DOUBLE NOT NULL COMMENT '传感器读数值',
     unit VARCHAR(16) NULL DEFAULT NULL COMMENT '单位 (如: °C, %, lux)',
     location_id INT NULL DEFAULT NULL COMMENT '位置ID',
     relic_id INT NULL DEFAULT NULL COMMENT '关联的文物ID',
     timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
     is_abnormal TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否异常数据 (0: 正常, 1: 异常)',
     create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     PRIMARY KEY (id,timestamp),
     KEY idx_sensor_id (sensor_id),
     KEY idx_timestamp (timestamp),
     KEY idx_type (type),
     KEY idx_location_id (location_id),
     KEY idx_relic_id (relic_id),
     KEY idx_is_abnormal (is_abnormal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器原始数据表'
    PARTITION BY RANGE COLUMNS(timestamp)
        (
        PARTITION p_before VALUES LESS THAN ('2025-06-01'),
        PARTITION p202506 VALUES LESS THAN ('2025-07-01'),
        PARTITION p202507 VALUES LESS THAN ('2025-08-01'),
        PARTITION p202508 VALUES LESS THAN ('2025-09-01'),
        PARTITION p202509 VALUES LESS THAN ('2025-10-01'),
        PARTITION p202510 VALUES LESS THAN ('2025-11-01'),
        PARTITION p202511 VALUES LESS THAN ('2025-12-01'),
        PARTITION p202512 VALUES LESS THAN ('2026-01-01'),
        PARTITION p_future VALUES LESS THAN (MAXVALUE)
        );


-- ----------------------------
-- Table structure for sensor_data_hourly (传感器数据小时聚合表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data_hourly`;
CREATE TABLE `sensor_data_hourly` (
       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
       `sensor_id` VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
       `type` VARCHAR(32) NOT NULL COMMENT '传感器类型',
       `min_value` DOUBLE NOT NULL COMMENT '最小值',
       `max_value` DOUBLE NOT NULL COMMENT '最大值',
       `avg_value` DOUBLE NOT NULL COMMENT '平均值',
       `std_dev` DOUBLE NULL DEFAULT NULL COMMENT '标准差',
       `sample_count` INT NOT NULL COMMENT '样本数量',
       `unit` VARCHAR(16) NULL DEFAULT NULL COMMENT '单位',
       `location_id` INT NULL DEFAULT NULL COMMENT '位置ID',
       `relic_id` INT NULL DEFAULT NULL COMMENT '关联的文物ID',
       `hour_timestamp` TIMESTAMP NOT NULL COMMENT '小时时间戳 (整点)',
       `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_sensor_hour` (`sensor_id`, `type`, `hour_timestamp`),
       KEY `idx_hour_timestamp` (`hour_timestamp`),
       KEY `idx_sensor_type` (`sensor_id`, `type`),
       KEY `idx_location_id` (`location_id`),
       KEY `idx_relic_id` (`relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器数据小时聚合表';

-- ----------------------------
-- Table structure for sensor_data_daily (传感器数据天聚合表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data_daily`;
CREATE TABLE `sensor_data_daily` (
       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
       `sensor_id` VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
       `type` VARCHAR(32) NOT NULL COMMENT '传感器类型',
       `min_value` DOUBLE NOT NULL COMMENT '最小值',
       `max_value` DOUBLE NOT NULL COMMENT '最大值',
       `avg_value` DOUBLE NOT NULL COMMENT '平均值',
       `std_dev` DOUBLE NULL DEFAULT NULL COMMENT '标准差',
       `sample_count` INT NOT NULL COMMENT '样本数量',
       `unit` VARCHAR(16) NULL DEFAULT NULL COMMENT '单位',
       `location_id` INT NULL DEFAULT NULL COMMENT '位置ID',
       `relic_id` INT NULL DEFAULT NULL COMMENT '关联的文物ID',
       `day_timestamp` DATE NOT NULL COMMENT '日期',
       `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_sensor_day` (`sensor_id`, `type`, `day_timestamp`),
       KEY `idx_day_timestamp` (`day_timestamp`),
       KEY `idx_sensor_type` (`sensor_id`, `type`),
       KEY `idx_location_id` (`location_id`),
       KEY `idx_relic_id` (`relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器数据天聚合表';
-- MySQL建表语句
-- 生成时间: 2026-04-19

-- api_account表
CREATE TABLE IF NOT EXISTS `api_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) DEFAULT NULL,
  `password` VARCHAR(255) DEFAULT NULL,
  `nickname` VARCHAR(255) DEFAULT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `create_date_time` DATETIME DEFAULT NULL,
  `update_date_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_account_1` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_access_log表
CREATE TABLE IF NOT EXISTS `api_access_log` (
  `id` VARCHAR(36) NOT NULL,
  `access_time` DATETIME NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `client_id` VARCHAR(255) NOT NULL,
  `params` TEXT DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_api_access_log_access_time` (`access_time`),
  KEY `idx_api_access_log_path` (`path`),
  KEY `idx_api_access_log_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_access_log_archive表
CREATE TABLE IF NOT EXISTS `api_access_log_archive` (
  `id` VARCHAR(36) NOT NULL,
  `access_time` DATETIME NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `client_id` VARCHAR(255) NOT NULL,
  `params` TEXT DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  `archive_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_api_access_log_archive_access_time` (`access_time`),
  KEY `idx_api_access_log_archive_path` (`path`),
  KEY `idx_api_access_log_archive_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_client表
CREATE TABLE IF NOT EXISTS `api_client` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `client_id` VARCHAR(255) DEFAULT NULL,
  `secret` VARCHAR(255) DEFAULT NULL,
  `expire_at` VARCHAR(255) DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_api_client_1` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_datasource表
CREATE TABLE IF NOT EXISTS `api_datasource` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `url` VARCHAR(255) DEFAULT NULL,
  `username` VARCHAR(255) DEFAULT NULL,
  `password` VARCHAR(255) DEFAULT NULL,
  `type` VARCHAR(255) DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_db_datasource_1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_config表
CREATE TABLE IF NOT EXISTS `api_config` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `path` VARCHAR(255) DEFAULT NULL,
  `datasource_id` VARCHAR(36) DEFAULT NULL,
  `sql_param` TEXT DEFAULT NULL,
  `status` INT DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_api_config_1` (`path`),
  KEY `fk_api_config_datasource` (`datasource_id`),
  CONSTRAINT `fk_api_config_datasource` FOREIGN KEY (`datasource_id`) REFERENCES `api_datasource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_field_mapping表
CREATE TABLE IF NOT EXISTS `api_field_mapping` (
  `id` VARCHAR(36) NOT NULL,
  `api_config_id` VARCHAR(36) NOT NULL,
  `field_name` VARCHAR(100) NOT NULL,
  `display_name` VARCHAR(200) NOT NULL,
  `column_width` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_api_field_mapping_1` (`api_config_id`),
  CONSTRAINT `fk_api_field_mapping_config` FOREIGN KEY (`api_config_id`) REFERENCES `api_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_config_access表
CREATE TABLE IF NOT EXISTS `api_config_access` (
  `id` VARCHAR(36) NOT NULL,
  `client_id` VARCHAR(36) DEFAULT NULL,
  `api_config_id` VARCHAR(36) DEFAULT NULL,
  `account_id` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_api_config_access_1` (`client_id`, `api_config_id`),
  KEY `fk_api_config_access_client` (`client_id`),
  KEY `fk_api_config_access_config` (`api_config_id`),
  CONSTRAINT `fk_api_config_access_client` FOREIGN KEY (`client_id`) REFERENCES `api_client` (`id`),
  CONSTRAINT `fk_api_config_access_config` FOREIGN KEY (`api_config_id`) REFERENCES `api_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_plugin表
CREATE TABLE IF NOT EXISTS `api_plugin` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `class_name` VARCHAR(255) NOT NULL,
  `account_id` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_api_plugin_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- api_config_plugin表
CREATE TABLE IF NOT EXISTS `api_config_plugin` (
  `id` VARCHAR(36) NOT NULL,
  `api_config_id` VARCHAR(36) NOT NULL,
  `api_plugin_id` VARCHAR(36) NOT NULL,
  `account_id` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_api_config_plugin_api_config_id` (`api_config_id`),
  KEY `idx_api_config_plugin_api_plugin_id` (`api_plugin_id`),
  KEY `idx_api_config_plugin_account_id` (`account_id`),
  CONSTRAINT `fk_api_config_plugin_api_config` FOREIGN KEY (`api_config_id`) REFERENCES `api_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_api_config_plugin_api_plugin` FOREIGN KEY (`api_plugin_id`) REFERENCES `api_plugin` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
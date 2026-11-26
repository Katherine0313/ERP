-- =====================================================================
-- ERP 初始化脚本（登录功能最小集：只建 admin 表）
--
-- 用法：在 IDEA 的 Database 面板里连上 MySQL，整份执行。
-- 注意：本脚本需要 CREATE USER / GRANT 权限（本机是 root），
--       应用层用的 erp_db 账号本身没有这些权限。
--
-- 初始管理员：admin / admin123
-- 密码以明文入库，首次登录成功后由后端自动升级为 BCrypt 密文
-- （见 AdminLoginServiceImpl 中 isEncoded 的兼容逻辑）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 建库 + 建应用账号
--    本机 MySQL 原本只有 root，erp_db 账号不存在，导致应用连不上库。
--    CREATE USER IF NOT EXISTS + ALTER USER 让脚本可重复执行。
--
--    注意：下面的密码是占位符，执行前请先改成你自己的密码，
--    并且不要把真实密码提交到版本库 —— 改完记得同步
--    src/main/resources/application-dev.yml 里的 DB_USERNAME / DB_PASSWORD。
-- ---------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS erp_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'erp_db'@'%' IDENTIFIED BY '改成你自己的密码';
ALTER USER 'erp_db'@'%' IDENTIFIED BY '改成你自己的密码';
GRANT ALL PRIVILEGES ON erp_db.* TO 'erp_db'@'%';
FLUSH PRIVILEGES;

USE erp_db;

-- ---------------------------------------------------------------------
-- 2. 管理员表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`     VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`     VARCHAR(100) NOT NULL COMMENT '密码，BCrypt 密文；历史明文账号首次登录后自动升级',
    `name`         VARCHAR(50)           DEFAULT NULL COMMENT '姓名',
    `phone`        VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `admin_avatar` VARCHAR(255)          DEFAULT NULL COMMENT '头像 URL',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=停用',
    `del_flag`     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除：0=正常 1=已删除',
    `create_time`  DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_time`  DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='管理员';

-- ---------------------------------------------------------------------
-- 3. 初始管理员（密码明文 admin123，首次登录后自动升级为 BCrypt）
-- ---------------------------------------------------------------------
INSERT INTO `admin` (`username`, `password`, `name`, `phone`, `status`, `create_time`, `update_time`)
VALUES ('admin', 'admin123', '超级管理员', '13800000000', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

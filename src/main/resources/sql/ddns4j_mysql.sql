SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS ddns4j CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ddns4j;

CREATE TABLE IF NOT EXISTS `job_task`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务分组',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务表达式',
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务类名',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务描述',
  `status` int(1) NULL DEFAULT 0 COMMENT '任务状态：0-停止，1-运行',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务调度表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `parsing_record`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `service_provider` int(11) NOT NULL COMMENT '服务提供商1 阿里云 2 腾讯云 3 cloudflare',
  `service_provider_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `service_provider_secret` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `record_type` int(11) NOT NULL COMMENT '解析类型:1 AAAA 2 A',
  `get_ip_mode` int(11) NOT NULL COMMENT '获取ip方式: 1 interface 2 network 3 cmd',
  `get_ip_mode_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当为interface时 https://myip4.ipip.net, https://ddns.oray.com/checkip, https://ip.3322.net, https://4.ipw.cn\r\n当为network时 是网卡信息\r\n当为cmd时 是bash或shell命令\r\n',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '真实的公网ip',
  `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '域名',
  `state` int(11) NOT NULL COMMENT '1 启用 0 禁用',
  `update_frequency` int(11) NOT NULL COMMENT '单位:分钟 1分钟 2分钟 5分钟 10分钟',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `creator` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
  `updater` bigint(20) NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '解析记录表' ROW_FORMAT = Dynamic;



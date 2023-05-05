
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for job_task
-- ----------------------------
DROP TABLE IF EXISTS `job_task`;
CREATE TABLE `job_task`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务分组',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务表达式',
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务类名',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务描述',
  `status` int(1) NULL DEFAULT 0 COMMENT '任务状态：0-停止，1-运行',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of job_task
-- ----------------------------

-- ----------------------------
-- Table structure for parsing_record
-- ----------------------------
DROP TABLE IF EXISTS `parsing_record`;
CREATE TABLE `parsing_record`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 1654354705997905922 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '解析记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parsing_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `dict_type_id` bigint(20) NOT NULL COMMENT '字典类型ID',
  `dict_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典标签',
  `dict_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典值',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '排序',
  `creator` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint(20) NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_value`(`dict_type_id`, `dict_value`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '阿里云', '1', '阿里云', 0, 0, '2023-03-19 00:23:47', 0, '2023-03-19 00:23:50');
INSERT INTO `sys_dict_data` VALUES (2, 1, '腾讯云', '2', '腾讯云', 0, 0, '2023-03-19 00:24:15', 0, '2023-03-19 00:24:24');
INSERT INTO `sys_dict_data` VALUES (3, 1, 'cloudflare', '3', 'cloudflare', 0, 0, '2023-03-19 00:24:48', 0, '2023-03-19 00:24:51');
INSERT INTO `sys_dict_data` VALUES (4, 2, 'AAAA(ipv6)', '1', 'AAAA(ipv6)', 0, 0, '2023-03-19 00:29:21', 0, '2023-03-19 00:29:26');
INSERT INTO `sys_dict_data` VALUES (5, 2, 'A(ipv4)', '2', 'A(ipv4)', 0, 0, '2023-03-19 00:29:59', 0, '2023-03-19 00:30:01');
INSERT INTO `sys_dict_data` VALUES (6, 3, '接口获取(interface)', '1', '接口获取(interface)', 0, 0, '2023-03-19 00:31:42', 0, '2023-03-19 00:31:45');
INSERT INTO `sys_dict_data` VALUES (7, 3, '网卡获取(network)', '2', '网卡获取(network)', 0, 0, '2023-03-19 00:32:18', 0, '2023-03-19 00:32:20');
INSERT INTO `sys_dict_data` VALUES (8, 3, '命令方式获取(cmd)', '3', '命令方式获取(cmd)', 0, 0, '2023-03-19 00:37:26', 0, '2023-03-19 00:37:28');
INSERT INTO `sys_dict_data` VALUES (9, 4, '1分钟', '1', '更新频率(分钟)', 0, 0, '2023-03-19 00:56:08', 0, '2023-03-19 00:56:14');
INSERT INTO `sys_dict_data` VALUES (10, 4, '2分钟', '2', '更新频率(分钟)', 0, 0, '2023-03-19 00:56:08', 0, '2023-03-19 00:56:14');
INSERT INTO `sys_dict_data` VALUES (11, 4, '5分钟', '5', '更新频率(分钟)', 0, 0, '2023-03-19 00:58:21', 0, '2023-03-19 00:58:27');
INSERT INTO `sys_dict_data` VALUES (12, 4, '10分钟', '10', '更新频率(分钟)', 0, 0, '2023-03-19 00:58:21', 0, '2023-03-19 00:58:27');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典类型',
  `dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '排序',
  `creator` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint(20) NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 'service_provider', '服务商', '域名解析服务商', 0, 0, '2023-03-19 00:19:11', 0, '2023-03-19 00:19:14');
INSERT INTO `sys_dict_type` VALUES (2, 'record_type', '记录类型', '域名解析记录类型', 0, 0, '2023-03-19 00:20:21', 0, '2023-03-19 00:20:24');
INSERT INTO `sys_dict_type` VALUES (3, 'get_ip_mode', '获取IP方式', '获取IP方式', 0, 0, '2023-03-19 00:21:12', 0, '2023-03-19 00:21:15');
INSERT INTO `sys_dict_type` VALUES (4, 'update_frequency', '更新频率', '更新频率', 0, 0, '2023-03-19 00:55:27', 0, '2023-03-19 00:55:32');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 山竹收集箱 Todo 表
-- 执行方式：在 MySQL 客户端中执行
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for shanzhu_todo
-- ----------------------------
DROP TABLE IF EXISTS `shanzhu_todo`;
CREATE TABLE `shanzhu_todo` (
  `id` bigint NOT NULL COMMENT 'TodoID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint DEFAULT NULL COMMENT '关联目标ID，可为空',
  `sub_goal_id` bigint DEFAULT NULL COMMENT '关联子目标ID，可为空',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Todo标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'Todo说明',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'inbox' COMMENT '状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `planned_date` date DEFAULT NULL COMMENT '计划处理日期',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `estimated_minutes` int DEFAULT NULL COMMENT '预计耗时，单位分钟',
  `actual_minutes` int DEFAULT NULL COMMENT '实际耗时，单位分钟',
  `task_id` bigint DEFAULT NULL COMMENT '关联任务ID（转为任务后填充）',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_id` bigint DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '最近一次更新人id',
  `update_time` datetime DEFAULT NULL COMMENT '最近一次更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shanzhu_todo_user_id` (`user_id`) USING BTREE,
  KEY `idx_shanzhu_todo_goal_id` (`goal_id`) USING BTREE,
  KEY `idx_shanzhu_todo_sub_goal_id` (`sub_goal_id`) USING BTREE,
  KEY `idx_shanzhu_todo_status` (`status`) USING BTREE,
  KEY `idx_shanzhu_todo_planned_date` (`planned_date`) USING BTREE,
  KEY `idx_shanzhu_todo_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='山竹收集箱Todo表';

-- ----------------------------
-- Records of shanzhu_todo
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

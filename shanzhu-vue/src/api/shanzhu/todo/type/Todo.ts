export interface ShanzhuTodo {
  /**
   * TodoID
   */
  id?: string;
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 关联目标ID，可为空
   */
  goalId?: string;
  /**
   * 关联子目标ID，可为空
   */
  subGoalId?: string;
  /**
   * Todo标题
   */
  title?: string;
  /**
   * Todo说明
   */
  description?: string;
  /**
   * 状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务
   */
  status?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 计划处理日期
   */
  plannedDate?: string;
  /**
   * 截止时间
   */
  deadline?: string;
  /**
   * 完成时间
   */
  completedTime?: string;
  /**
   * 预计耗时，单位分钟
   */
  estimatedMinutes?: number;
  /**
   * 实际耗时，单位分钟
   */
  actualMinutes?: number;
  /**
   * 关联任务ID（转为任务后填充）
   */
  taskId?: string;
  /**
   * 排序
   */
  sortOrder?: number;
  /**
   * 备注
   */
  remark?: string;
}

export interface ShanzhuTodoVO extends ShanzhuTodo {
  /**
   * 目标名称
   */
  goalTitle?: string;
  /**
   * 子目标名称
   */
  subGoalTitle?: string;
  /**
   * 创建时间
   */
  createTime?: string;
}

export interface ShanzhuTodoQuery {
  /**
   * 关键词
   */
  keyword?: string;
  /**
   * 状态
   */
  status?: string;
  /**
   * 关联目标ID
   */
  goalId?: string;
  /**
   * 关联子目标ID
   */
  subGoalId?: string;
  /**
   * 计划处理日期
   */
  plannedDate?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 当前页数
   */
  pageNum?: number;
  /**
   * 每页记录数
   */
  pageSize?: number;
}

export interface TodoStatusOption {
  label: string;
  value: string;
  color: string;
}

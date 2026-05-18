export interface ShanzhuTask {
  /**
   * 任务ID
   */
  id?: string;
  /**
   * 所属目标ID
   */
  goalId?: string;
  /**
   * 所属子目标ID，可为空
   */
  subGoalId?: string;
  /**
   * 任务标题
   */
  title?: string;
  /**
   * 任务说明
   */
  description?: string;
  /**
   * 任务状态
   */
  status?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 计划执行日期
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
   * 排序
   */
  sortOrder?: number;
}

export interface ShanzhuTaskVO extends ShanzhuTask {
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 创建时间
   */
  createTime?: string;
}

export interface ShanzhuTaskQuery {
  /**
   * 所属目标ID
   */
  goalId: string;
  /**
   * 所属子目标ID，可为空
   */
  subGoalId?: string;
  /**
   * 任务状态
   */
  status?: string;
  /**
   * 计划执行日期
   */
  plannedDate?: string;
}

export interface ShanzhuTaskStatus {
  /**
   * 任务ID
   */
  id: string;
  /**
   * 任务状态
   */
  status: string;
}

export interface ShanzhuTaskSort {
  /**
   * 任务ID
   */
  id: string;
  /**
   * 排序
   */
  sortOrder: number;
}

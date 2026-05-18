export interface ShanzhuGoalProgress {
  /**
   * 主键
   */
  id?: string;
  /**
   * 目标ID
   */
  goalId?: string;
  /**
   * 子目标ID，可为空
   */
  subGoalId?: string;
  /**
   * 任务ID，可为空
   */
  taskId?: string;
  /**
   * 进展标题
   */
  title?: string;
  /**
   * 进展内容
   */
  content?: string;
  /**
   * 变更前进度
   */
  progressBefore?: number;
  /**
   * 变更后进度
   */
  progressAfter?: number;
  /**
   * 记录日期
   */
  recordDate?: string;
}

export interface ShanzhuGoalProgressVO extends ShanzhuGoalProgress {
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 创建时间
   */
  createTime?: string;
}

export interface ShanzhuGoalProgressQuery {
  /**
   * 目标ID
   */
  goalId: string;
}

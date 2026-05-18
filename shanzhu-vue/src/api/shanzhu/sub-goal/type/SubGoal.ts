import type {ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";

export interface ShanzhuSubGoal {
  /**
   * 子目标ID
   */
  id?: string;
  /**
   * 所属目标ID
   */
  goalId?: string;
  /**
   * 子目标名称
   */
  title?: string;
  /**
   * 子目标描述
   */
  description?: string;
  /**
   * 子目标状态
   */
  status?: string;
  /**
   * 子目标进度，0-100
   */
  progress?: number;
  /**
   * 权重
   */
  weight?: number;
  /**
   * 排序
   */
  sortOrder?: number;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 截止日期
   */
  deadline?: string;
  /**
   * 完成时间
   */
  completedTime?: string;
}

export interface ShanzhuSubGoalVO extends ShanzhuSubGoal {
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 创建时间
   */
  createTime?: string;
  /**
   * 子目标下任务列表
   */
  tasks?: ShanzhuTaskVO[];
}

export interface ShanzhuSubGoalQuery {
  /**
   * 所属目标ID
   */
  goalId: string;
}

export interface ShanzhuSubGoalStatus {
  /**
   * 子目标ID
   */
  id: string;
  /**
   * 子目标状态
   */
  status: string;
}

export interface ShanzhuSubGoalProgress {
  /**
   * 子目标ID
   */
  id: string;
  /**
   * 子目标进度，0-100
   */
  progress: number;
}

export interface ShanzhuSubGoalSort {
  /**
   * 子目标ID
   */
  id: string;
  /**
   * 排序
   */
  sortOrder: number;
}

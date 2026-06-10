import type {ShanzhuTag} from "@/api/shanzhu/tag/type/Tag.ts";

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
  /**
   * 分类ID
   */
  categoryId?: string;
  /**
   * 标签ID列表
   */
  tagIds?: string[];
}

export interface ShanzhuTaskVO extends ShanzhuTask {
  /**
   * 目标名称
   */
  goalTitle?: string;
  /**
   * 分类ID
   */
  categoryId?: string;
  /**
   * 分类名称
   */
  categoryName?: string;
  /**
   * 分类颜色
   */
  categoryColor?: string;
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 创建时间
   */
  createTime?: string;
  /**
   * 标签列表
   */
  tags?: ShanzhuTag[];
}

export interface ShanzhuTaskQuery {
  /**
   * 关键词
   */
  keyword?: string;
  /**
   * 所属目标ID
   */
  goalId?: string;
  /**
   * 目标分类ID
   */
  categoryId?: string;
  /**
   * 标签ID列表
   */
  tagIds?: string[];
  /**
   * 所属子目标ID，可为空
   */
  subGoalId?: string;
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
   * 查询类型：today 今日任务，overdue 逾期任务
   */
  queryType?: string;
  /**
   * 当前页数
   */
  pageNum?: number;
  /**
   * 每页记录数
   */
  pageSize?: number;
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

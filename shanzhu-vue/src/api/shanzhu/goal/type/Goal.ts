import type {ShanzhuTag} from "@/api/shanzhu/tag/type/Tag.ts";

export interface ShanzhuGoal {
  /**
   * 目标ID
   */
  id?: string;
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 分类ID
   */
  categoryId?: string;
  /**
   * 目标名称
   */
  title?: string;
  /**
   * 目标描述
   */
  description?: string;
  /**
   * 目标类型
   */
  goalType?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 目标状态
   */
  status?: string;
  /**
   * 当前进度，0-100
   */
  progress?: number;
  /**
   * 进度模式
   */
  progressMode?: string;
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
  /**
   * 备注
   */
  remark?: string;
  /**
   * 标签ID列表
   */
  tagIds?: string[];
}

export interface ShanzhuGoalVO extends ShanzhuGoal {
  /**
   * 分类名称
   */
  categoryName?: string;
  /**
   * 分类颜色
   */
  categoryColor?: string;
  /**
   * 创建时间
   */
  createTime?: string;
  /**
   * 标签列表
   */
  tags?: ShanzhuTag[];
}

export interface ShanzhuGoalQuery {
  /**
   * 关键词
   */
  keyword?: string;
  /**
   * 分类ID
   */
  categoryId?: string;
  /**
   * 目标状态
   */
  status?: string;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 截止日期
   */
  deadline?: string;
  /**
   * 当前页数
   */
  pageNum: number;
  /**
   * 每页记录数
   */
  pageSize: number;
}

export interface GoalStatusOption {
  label: string;
  value: string;
  color: string;
}

export interface GoalPriorityOption {
  label: string;
  value: number;
  color: string;
}

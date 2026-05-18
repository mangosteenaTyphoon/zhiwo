export interface ShanzhuReviewQuery {
  /**
   * 复盘类型：week 周复盘，month 月复盘
   */
  reviewType?: string;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 结束日期
   */
  endDate?: string;
}

export interface ShanzhuReview {
  /**
   * 复盘类型
   */
  reviewType: string;
  /**
   * 复盘开始日期
   */
  startDate: string;
  /**
   * 复盘结束日期
   */
  endDate: string;
  /**
   * 复盘概览
   */
  overview: ReviewOverview;
  /**
   * 分类投入分析
   */
  categoryInvestmentStats: CategoryInvestmentStats[];
  /**
   * 标签维度分析
   */
  tagAnalysisStats: TagAnalysisStats[];
  /**
   * 任务效率趋势
   */
  taskEfficiencyTrends: TaskEfficiencyTrend[];
  /**
   * 目标达成分析
   */
  goalAchievementStats: GoalAchievementStats[];
  /**
   * 目标调整建议
   */
  adjustmentSuggestions: AdjustmentSuggestion[];
  /**
   * 复盘摘要
   */
  reviewSummary: string[];
}

export interface ReviewOverview {
  /**
   * 完成任务数
   */
  completedTaskCount: number;
  /**
   * 新增进展记录数
   */
  progressRecordCount: number;
  /**
   * 推进目标数
   */
  progressedGoalCount: number;
  /**
   * 完成目标数
   */
  completedGoalCount: number;
  /**
   * 总预计耗时，单位分钟
   */
  totalEstimatedMinutes: number;
  /**
   * 总实际耗时，单位分钟
   */
  totalActualMinutes: number;
  /**
   * 平均任务完成效率
   */
  averageEfficiencyRate: number;
}

export interface CategoryInvestmentStats {
  /**
   * 分类ID
   */
  categoryId?: string;
  /**
   * 分类名称
   */
  categoryName: string;
  /**
   * 分类颜色
   */
  categoryColor?: string;
  /**
   * 完成任务数
   */
  completedTaskCount: number;
  /**
   * 实际投入分钟数
   */
  actualMinutes: number;
  /**
   * 投入占比
   */
  investmentRatio: number;
  /**
   * 完成目标数
   */
  completedGoalCount: number;
}

export interface TagAnalysisStats {
  /**
   * 标签ID
   */
  tagId?: string;
  /**
   * 标签名称
   */
  tagName: string;
  /**
   * 标签颜色
   */
  tagColor?: string;
  /**
   * 标签类型
   */
  tagType?: string;
  /**
   * 关联任务数
   */
  taskCount: number;
  /**
   * 已完成任务数
   */
  completedTaskCount: number;
  /**
   * 完成率
   */
  completionRate: number;
  /**
   * 实际投入分钟数
   */
  actualMinutes: number;
}

export interface TaskEfficiencyTrend {
  /**
   * 日期
   */
  date: string;
  /**
   * 完成任务数
   */
  completedTaskCount: number;
  /**
   * 预计耗时，单位分钟
   */
  estimatedMinutes: number;
  /**
   * 实际耗时，单位分钟
   */
  actualMinutes: number;
  /**
   * 效率比
   */
  efficiencyRate: number;
}

export interface GoalAchievementStats {
  /**
   * 目标ID
   */
  goalId: string;
  /**
   * 目标名称
   */
  goalTitle: string;
  /**
   * 分类名称
   */
  categoryName?: string;
  /**
   * 分类颜色
   */
  categoryColor?: string;
  /**
   * 目标状态
   */
  status?: string;
  /**
   * 当前进度
   */
  progress?: number;
  /**
   * 复盘期内进展记录数量
   */
  progressRecordCount: number;
  /**
   * 复盘期内完成任务数量
   */
  completedTaskCount: number;
  /**
   * 是否建议调整
   */
  needAdjustment: boolean;
  /**
   * 调整原因
   */
  adjustmentReason?: string;
}

export interface AdjustmentSuggestion {
  /**
   * 目标ID
   */
  goalId: string;
  /**
   * 目标名称
   */
  goalTitle: string;
  /**
   * 建议类型
   */
  suggestionType: string;
  /**
   * 建议内容
   */
  suggestionContent: string;
}

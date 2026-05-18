export interface ShanzhuDashboard {
  /**
   * 概览统计
   */
  overview: DashboardOverviewStats;
  /**
   * 分类维度统计
   */
  categoryStats: DashboardCategoryStats[];
  /**
   * 时间维度统计
   */
  timeStats: DashboardTimeStats;
  /**
   * 风险统计
   */
  riskStats: DashboardRiskStats;
}

export interface DashboardOverviewStats {
  /**
   * 目标总数
   */
  totalGoalCount: number;
  /**
   * 进行中目标数量
   */
  inProgressGoalCount: number;
  /**
   * 已完成目标数量
   */
  completedGoalCount: number;
  /**
   * 暂停目标数量
   */
  pausedGoalCount: number;
  /**
   * 放弃目标数量
   */
  abandonedGoalCount: number;
  /**
   * 任务总数
   */
  totalTaskCount: number;
  /**
   * 待处理任务数量
   */
  pendingTaskCount: number;
  /**
   * 已完成任务数量
   */
  completedTaskCount: number;
  /**
   * 逾期目标数量
   */
  overdueGoalCount: number;
  /**
   * 逾期任务数量
   */
  overdueTaskCount: number;
  /**
   * 长时间未推进目标数量
   */
  longTimeNoProgressGoalCount: number;
  /**
   * 高优先级未完成任务数量
   */
  highPriorityUnfinishedTaskCount: number;
}

export interface DashboardCategoryStats {
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
   * 目标数量
   */
  goalCount: number;
  /**
   * 已完成目标数量
   */
  completedGoalCount: number;
  /**
   * 任务数量
   */
  taskCount: number;
  /**
   * 已完成任务数量
   */
  completedTaskCount: number;
}

export interface DashboardTimeStats {
  /**
   * 今日完成任务数
   */
  todayCompletedTaskCount: number;
  /**
   * 本周完成任务数
   */
  weekCompletedTaskCount: number;
  /**
   * 本月完成任务数
   */
  monthCompletedTaskCount: number;
  /**
   * 最近7天任务完成趋势
   */
  recentSevenDayTaskCompletionTrend: DashboardDailyTaskCompletionStats[];
}

export interface DashboardDailyTaskCompletionStats {
  /**
   * 日期
   */
  date: string;
  /**
   * 完成任务数
   */
  completedTaskCount: number;
}

export interface DashboardRiskStats {
  /**
   * 逾期目标
   */
  overdueGoals: DashboardRiskGoal[];
  /**
   * 逾期任务
   */
  overdueTasks: DashboardRiskTask[];
  /**
   * 长时间未推进目标
   */
  longTimeNoProgressGoals: DashboardRiskGoal[];
  /**
   * 高优先级未完成任务
   */
  highPriorityUnfinishedTasks: DashboardRiskTask[];
}

export interface DashboardRiskGoal {
  /**
   * 目标ID
   */
  id: string;
  /**
   * 目标名称
   */
  title: string;
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
   * 目标状态
   */
  status?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 当前进度
   */
  progress?: number;
  /**
   * 截止日期
   */
  deadline?: string;
  /**
   * 逾期天数
   */
  overdueDays: number;
  /**
   * 最近推进日期
   */
  lastProgressDate?: string;
  /**
   * 距最近推进天数
   */
  daysSinceLastProgress: number;
}

export interface DashboardRiskTask {
  /**
   * 任务ID
   */
  id: string;
  /**
   * 任务标题
   */
  title: string;
  /**
   * 所属目标ID
   */
  goalId?: string;
  /**
   * 所属目标名称
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
   * 任务状态
   */
  status?: string;
  /**
   * 优先级
   */
  priority?: number;
  /**
   * 截止日期
   */
  deadline?: string;
  /**
   * 逾期天数
   */
  overdueDays: number;
}

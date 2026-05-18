export interface ShanzhuHabit {
  /**
   * 习惯ID
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
   * 习惯名称
   */
  title?: string;
  /**
   * 习惯说明
   */
  description?: string;
  /**
   * 频率类型：daily/weekly/monthly
   */
  frequencyType?: string;
  /**
   * 周期目标次数
   */
  targetCount?: number;
  /**
   * 每次目标值
   */
  targetValue?: number;
  /**
   * 单位
   */
  unit?: string;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 结束日期
   */
  endDate?: string;
  /**
   * 状态：active/paused/completed/cancelled
   */
  status?: string;
  /**
   * 排序
   */
  sortOrder?: number;
  /**
   * 备注
   */
  remark?: string;
}

export interface ShanzhuHabitVO extends ShanzhuHabit {
  /**
   * 目标名称
   */
  goalTitle?: string;
  /**
   * 子目标名称
   */
  subGoalTitle?: string;
  /**
   * 今日是否已打卡
   */
  todayChecked?: boolean;
  /**
   * 今日打卡记录ID
   */
  todayCheckinId?: string;
  /**
   * 当前周期已打卡次数
   */
  currentPeriodCheckedCount?: number;
  /**
   * 当前周期目标次数
   */
  currentPeriodTargetCount?: number;
  /**
   * 当前周期完成率
   */
  currentPeriodCompletionRate?: number;
  /**
   * 连续打卡天数
   */
  continuousDays?: number;
  /**
   * 最近打卡记录
   */
  recentCheckins?: ShanzhuHabitCheckinVO[];
  /**
   * 创建时间
   */
  createTime?: string;
}

export interface ShanzhuHabitQuery {
  /**
   * 关键词
   */
  keyword?: string;
  /**
   * 关联目标ID
   */
  goalId?: string;
  /**
   * 关联子目标ID
   */
  subGoalId?: string;
  /**
   * 状态
   */
  status?: string;
  /**
   * 频率类型
   */
  frequencyType?: string;
  /**
   * 当前页数
   */
  pageNum?: number;
  /**
   * 每页记录数
   */
  pageSize?: number;
}

export interface ShanzhuHabitCheckin {
  /**
   * 打卡记录ID
   */
  id?: string;
  /**
   * 习惯ID
   */
  habitId?: string;
  /**
   * 打卡日期
   */
  checkinDate?: string;
  /**
   * 实际完成值
   */
  actualValue?: number;
  /**
   * 打卡备注
   */
  note?: string;
  /**
   * 心情
   */
  mood?: string;
}

export interface ShanzhuHabitCheckinVO extends ShanzhuHabitCheckin {
  /**
   * 目标ID
   */
  goalId?: string;
  /**
   * 子目标ID
   */
  subGoalId?: string;
  /**
   * 习惯名称
   */
  habitTitle?: string;
  /**
   * 目标名称
   */
  goalTitle?: string;
  /**
   * 子目标名称
   */
  subGoalTitle?: string;
  /**
   * 单位
   */
  unit?: string;
  /**
   * 状态
   */
  status?: string;
  /**
   * 创建时间
   */
  createTime?: string;
}

export interface ShanzhuHabitCheckinQuery {
  /**
   * 习惯ID
   */
  habitId?: string;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 结束日期
   */
  endDate?: string;
}

export interface ShanzhuHabitTodayVO {
  /**
   * 习惯ID
   */
  id?: string;
  /**
   * 习惯名称
   */
  title?: string;
  /**
   * 每次目标值
   */
  targetValue?: number;
  /**
   * 单位
   */
  unit?: string;
  /**
   * 目标ID
   */
  goalId?: string;
  /**
   * 目标名称
   */
  goalTitle?: string;
  /**
   * 子目标ID
   */
  subGoalId?: string;
  /**
   * 子目标名称
   */
  subGoalTitle?: string;
  /**
   * 今日是否已打卡
   */
  todayChecked?: boolean;
  /**
   * 今日打卡记录ID
   */
  todayCheckinId?: string;
  /**
   * 今日实际完成值
   */
  actualValue?: number;
  /**
   * 今日备注
   */
  note?: string;
}

export interface ShanzhuHabitStatsVO {
  /**
   * 今日待打卡数量
   */
  todayTotalCount?: number;
  /**
   * 今日已打卡数量
   */
  todayCheckedCount?: number;
  /**
   * 当前周期习惯数量
   */
  currentPeriodHabitCount?: number;
  /**
   * 当前周期已完成习惯数量
   */
  currentPeriodCompletedHabitCount?: number;
  /**
   * 当前周期未完成习惯数量
   */
  currentPeriodUncompletedHabitCount?: number;
  /**
   * 当前周期完成率
   */
  currentPeriodCompletionRate?: number;
  /**
   * 最大连续打卡天数
   */
  maxContinuousDays?: number;
}

export interface HabitStatusOption {
  label: string;
  value: string;
  color: string;
}

export interface HabitFrequencyOption {
  label: string;
  value: string;
}

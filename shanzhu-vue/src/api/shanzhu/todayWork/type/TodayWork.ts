import type {ShanzhuHabitTodayVO} from "@/api/shanzhu/habit/type/Habit.ts";
import type {ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";
import type {ShanzhuTodoVO} from "@/api/shanzhu/todo/type/Todo.ts";

export interface TodayOverview {
  /**
   * 今日任务总数
   */
  taskTotalCount?: number;
  /**
   * 今日已完成任务数
   */
  taskCompletedCount?: number;
  /**
   * 今日习惯总数
   */
  habitTotalCount?: number;
  /**
   * 今日已打卡习惯数
   */
  habitCheckedCount?: number;
  /**
   * 今日Todo总数
   */
  todoTotalCount?: number;
  /**
   * 今日已完成Todo数
   */
  todoCompletedCount?: number;
}

export interface ShanzhuTodayWorkVO {
  /**
   * 今日概览统计
   */
  overview?: TodayOverview;
  /**
   * 今日任务列表
   */
  tasks?: ShanzhuTaskVO[];
  /**
   * 今日习惯列表
   */
  habits?: ShanzhuHabitTodayVO[];
  /**
   * 今日Todo列表
   */
  todos?: ShanzhuTodoVO[];
}

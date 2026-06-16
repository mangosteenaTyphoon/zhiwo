import request from "./request";

export interface TaskVO {
  id: string;
  goalId: string;
  goalTitle: string;
  categoryId: string;
  categoryName: string;
  categoryColor: string;
  subGoalId: string;
  userId: string;
  title: string;
  description: string;
  status: string;
  priority: number;
  plannedDate: string;
  deadline: string;
  completedTime: string;
  estimatedMinutes: number;
  actualMinutes: number;
  sortOrder: number;
  createTime: string;
  tags: any[];
}

export interface TaskQuery {
  keyword?: string;
  goalId?: string;
  categoryId?: string;
  subGoalId?: string;
  status?: string;
  priority?: number;
  plannedDate?: string;
  queryType?: string; // today / overdue
  pageNum?: number;
  pageSize?: number;
}

export interface TaskSave {
  id?: string;
  goalId: string;
  subGoalId?: string;
  title: string;
  description?: string;
  status?: string;
  priority?: number;
  plannedDate?: string;
  deadline?: string;
  estimatedMinutes?: number;
  actualMinutes?: number;
  sortOrder?: number;
  tagIds?: string[];
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/** 分页查询任务 */
export function getTaskPage(query: TaskQuery): Promise<PageResult<TaskVO>> {
  return request({ url: "/shanzhu/task/page", method: "post", data: query });
}

/** 保存任务（新增/更新） */
export function saveTask(data: TaskSave): Promise<string> {
  return request({ url: "/shanzhu/task", method: "post", data });
}

/** 删除任务 */
export function deleteTask(id: string): Promise<void> {
  return request({ url: `/shanzhu/task/${id}`, method: "delete" });
}

/** 更新任务状态 */
export function updateTaskStatus(id: string, status: string): Promise<void> {
  return request({ url: "/shanzhu/task/status", method: "post", data: { id, status } });
}

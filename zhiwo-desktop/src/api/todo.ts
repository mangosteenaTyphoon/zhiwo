import request from "./request";

export interface TodoVO {
  id: string;
  userId: string;
  goalId: string;
  subGoalId: string;
  title: string;
  description: string;
  status: string;
  priority: number;
  plannedDate: string;
  deadline: string;
  completedTime: string;
  estimatedMinutes: number;
  actualMinutes: number;
  taskId: string;
  sortOrder: number;
  remark: string;
  goalTitle: string;
  subGoalTitle: string;
  createTime: string;
}

export interface TodoQuery {
  keyword?: string;
  status?: string;
  goalId?: string;
  subGoalId?: string;
  plannedDate?: string;
  priority?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface TodoSave {
  id?: string;
  goalId?: string;
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
  remark?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/** 分页查询 Todo 列表 */
export function getTodoPage(query: TodoQuery): Promise<PageResult<TodoVO>> {
  return request({ url: "/shanzhu/todo/page", method: "post", data: query });
}

/** 查询 Todo 列表（不分页） */
export function getTodoList(query: TodoQuery): Promise<TodoVO[]> {
  return request({ url: "/shanzhu/todo/list", method: "post", data: query });
}

/** 保存 Todo（新增/更新） */
export function saveTodo(data: TodoSave): Promise<string> {
  return request({ url: "/shanzhu/todo", method: "post", data });
}

/** 删除 Todo */
export function deleteTodo(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}`, method: "delete" });
}

/** 完成 Todo */
export function completeTodo(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/complete`, method: "post" });
}

/** 取消完成 Todo */
export function uncompleteTodo(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/uncomplete`, method: "post" });
}

/** 移入今日待办 */
export function moveToToday(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/today`, method: "post" });
}

/** 移回收集箱 */
export function moveToInbox(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/inbox`, method: "post" });
}

/** 归档 Todo */
export function archiveTodo(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/archive`, method: "post" });
}

/** Todo 转任务 */
export function convertTodoToTask(id: string): Promise<void> {
  return request({ url: `/shanzhu/todo/${id}/convert-task`, method: "post" });
}

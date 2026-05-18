import request from "@/utils/Request.ts";
import type {
  ShanzhuTask,
  ShanzhuTaskQuery,
  ShanzhuTaskSort,
  ShanzhuTaskStatus,
  ShanzhuTaskVO
} from "@/api/shanzhu/task/type/Task.ts";

/**
 * 查询任务列表
 * @param data
 */
export const queryTaskList = (data: ShanzhuTaskQuery) => {
  return request<Array<ShanzhuTaskVO>>({
    url: 'shanzhu/task/list',
    method: 'post',
    data: data
  })
}

/**
 * 保存任务
 * @param data
 */
export const saveTask = (data: ShanzhuTask) => {
  return request<string>({
    url: 'shanzhu/task',
    method: 'post',
    data: data
  })
}

/**
 * 删除任务
 * @param id
 */
export const deleteTask = (id: string) => {
  return request<string>({
    url: 'shanzhu/task/' + id,
    method: 'delete'
  })
}

/**
 * 更新任务状态
 * @param data
 */
export const updateTaskStatus = (data: ShanzhuTaskStatus) => {
  return request<string>({
    url: 'shanzhu/task/status',
    method: 'post',
    data: data
  })
}

/**
 * 调整任务排序
 * @param data
 */
export const updateTaskSort = (data: ShanzhuTaskSort) => {
  return request<string>({
    url: 'shanzhu/task/sort',
    method: 'post',
    data: data
  })
}

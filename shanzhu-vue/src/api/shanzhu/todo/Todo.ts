import request from "@/utils/Request.ts";
import type {PageResponseType} from "@/api/global/Type.ts";
import type {ShanzhuTodo, ShanzhuTodoQuery, ShanzhuTodoVO} from "@/api/shanzhu/todo/type/Todo.ts";

/**
 * 分页查询Todo列表
 * @param data
 */
export const queryTodoPage = (data: ShanzhuTodoQuery) => {
  return request<PageResponseType<ShanzhuTodoVO>>({
    url: 'shanzhu/todo/page',
    method: 'post',
    data: data
  })
}

/**
 * 查询Todo列表
 * @param data
 */
export const queryTodoList = (data: ShanzhuTodoQuery) => {
  return request<Array<ShanzhuTodoVO>>({
    url: 'shanzhu/todo/list',
    method: 'post',
    data: data
  })
}

/**
 * 保存Todo
 * @param data
 */
export const saveTodo = (data: ShanzhuTodo) => {
  return request<string>({
    url: 'shanzhu/todo',
    method: 'post',
    data: data
  })
}

/**
 * 删除Todo
 * @param id
 */
export const deleteTodo = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id,
    method: 'delete'
  })
}

/**
 * 完成Todo
 * @param id
 */
export const completeTodo = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id + '/complete',
    method: 'post'
  })
}

/**
 * 取消完成Todo
 * @param id
 */
export const uncompleteTodo = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id + '/uncomplete',
    method: 'post'
  })
}

/**
 * 移入今日待办
 * @param id
 */
export const moveToToday = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id + '/today',
    method: 'post'
  })
}

/**
 * 移回收集箱
 * @param id
 */
export const moveToInbox = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id + '/inbox',
    method: 'post'
  })
}

/**
 * 归档Todo
 * @param id
 */
export const archiveTodo = (id: string) => {
  return request<string>({
    url: 'shanzhu/todo/' + id + '/archive',
    method: 'post'
  })
}

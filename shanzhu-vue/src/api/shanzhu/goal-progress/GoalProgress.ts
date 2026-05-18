import request from "@/utils/Request.ts";
import type {
  ShanzhuGoalProgress,
  ShanzhuGoalProgressQuery,
  ShanzhuGoalProgressVO
} from "@/api/shanzhu/goal-progress/type/GoalProgress.ts";

/**
 * 查询目标进展记录
 * @param data
 */
export const queryGoalProgressList = (data: ShanzhuGoalProgressQuery) => {
  return request<Array<ShanzhuGoalProgressVO>>({
    url: 'shanzhu/goal-progress/list',
    method: 'post',
    data: data
  })
}

/**
 * 保存目标进展记录
 * @param data
 */
export const saveGoalProgress = (data: ShanzhuGoalProgress) => {
  return request<string>({
    url: 'shanzhu/goal-progress',
    method: 'post',
    data: data
  })
}

/**
 * 删除目标进展记录
 * @param id
 */
export const deleteGoalProgress = (id: string) => {
  return request<string>({
    url: 'shanzhu/goal-progress/' + id,
    method: 'delete'
  })
}

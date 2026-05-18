import request from "@/utils/Request.ts";
import type {
  ShanzhuSubGoal,
  ShanzhuSubGoalProgress,
  ShanzhuSubGoalQuery,
  ShanzhuSubGoalSort,
  ShanzhuSubGoalStatus,
  ShanzhuSubGoalVO
} from "@/api/shanzhu/sub-goal/type/SubGoal.ts";

/**
 * 查询目标下的子目标列表
 * @param data
 */
export const querySubGoalList = (data: ShanzhuSubGoalQuery) => {
  return request<Array<ShanzhuSubGoalVO>>({
    url: 'shanzhu/sub-goal/list',
    method: 'post',
    data: data
  })
}

/**
 * 保存子目标
 * @param data
 */
export const saveSubGoal = (data: ShanzhuSubGoal) => {
  return request<string>({
    url: 'shanzhu/sub-goal',
    method: 'post',
    data: data
  })
}

/**
 * 删除子目标
 * @param id
 */
export const deleteSubGoal = (id: string) => {
  return request<string>({
    url: 'shanzhu/sub-goal/' + id,
    method: 'delete'
  })
}

/**
 * 更新子目标状态
 * @param data
 */
export const updateSubGoalStatus = (data: ShanzhuSubGoalStatus) => {
  return request<string>({
    url: 'shanzhu/sub-goal/status',
    method: 'post',
    data: data
  })
}

/**
 * 更新子目标进度
 * @param data
 */
export const updateSubGoalProgress = (data: ShanzhuSubGoalProgress) => {
  return request<string>({
    url: 'shanzhu/sub-goal/progress',
    method: 'post',
    data: data
  })
}

/**
 * 调整子目标排序
 * @param data
 */
export const updateSubGoalSort = (data: ShanzhuSubGoalSort) => {
  return request<string>({
    url: 'shanzhu/sub-goal/sort',
    method: 'post',
    data: data
  })
}

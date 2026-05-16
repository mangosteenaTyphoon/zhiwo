import request from "@/utils/Request.ts";
import type {PageResponseType} from "@/api/global/Type.ts";
import type {ShanzhuGoal, ShanzhuGoalQuery, ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";

/**
 * 分页查询目标列表
 * @param data
 */
export const queryGoalPage = (data: ShanzhuGoalQuery) => {
  return request<PageResponseType<ShanzhuGoalVO>>({
    url: 'shanzhu/goal/page',
    method: 'post',
    data: data
  })
}

/**
 * 保存目标
 * @param data
 */
export const saveGoal = (data: ShanzhuGoal) => {
  return request<string>({
    url: 'shanzhu/goal',
    method: 'post',
    data: data
  })
}

/**
 * 根据ID查询目标详情
 * @param id
 */
export const queryGoalById = (id: string) => {
  return request<ShanzhuGoalVO>({
    url: 'shanzhu/goal/' + id,
    method: 'get'
  })
}

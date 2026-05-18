import request from "@/utils/Request.ts";
import type {PageResponseType} from "@/api/global/Type.ts";
import type {
  ShanzhuHabit,
  ShanzhuHabitCheckin,
  ShanzhuHabitCheckinQuery,
  ShanzhuHabitCheckinVO,
  ShanzhuHabitQuery,
  ShanzhuHabitStatsVO,
  ShanzhuHabitTodayVO,
  ShanzhuHabitVO
} from "@/api/shanzhu/habit/type/Habit.ts";

/**
 * 分页查询习惯列表
 * @param data
 */
export const queryHabitPage = (data: ShanzhuHabitQuery) => {
  return request<PageResponseType<ShanzhuHabitVO>>({
    url: 'shanzhu/habit/page',
    method: 'post',
    data: data
  })
}

/**
 * 查询习惯列表
 * @param data
 */
export const queryHabitList = (data: ShanzhuHabitQuery) => {
  return request<Array<ShanzhuHabitVO>>({
    url: 'shanzhu/habit/list',
    method: 'post',
    data: data
  })
}

/**
 * 保存习惯
 * @param data
 */
export const saveHabit = (data: ShanzhuHabit) => {
  return request<string>({
    url: 'shanzhu/habit',
    method: 'post',
    data: data
  })
}

/**
 * 删除习惯
 * @param id
 */
export const deleteHabit = (id: string) => {
  return request<string>({
    url: 'shanzhu/habit/' + id,
    method: 'delete'
  })
}

/**
 * 暂停习惯
 * @param id
 */
export const pauseHabit = (id: string) => {
  return request<string>({
    url: 'shanzhu/habit/' + id + '/pause',
    method: 'post'
  })
}

/**
 * 恢复习惯
 * @param id
 */
export const resumeHabit = (id: string) => {
  return request<string>({
    url: 'shanzhu/habit/' + id + '/resume',
    method: 'post'
  })
}

/**
 * 查询今日待打卡列表
 */
export const queryTodayHabits = () => {
  return request<Array<ShanzhuHabitTodayVO>>({
    url: 'shanzhu/habit/today',
    method: 'get'
  })
}

/**
 * 查询习惯统计
 */
export const queryHabitStats = () => {
  return request<ShanzhuHabitStatsVO>({
    url: 'shanzhu/habit/stats',
    method: 'get'
  })
}

/**
 * 打卡或修改打卡
 * @param data
 */
export const saveHabitCheckin = (data: ShanzhuHabitCheckin) => {
  return request<string>({
    url: 'shanzhu/habit/checkin',
    method: 'post',
    data: data
  })
}

/**
 * 取消打卡
 * @param id
 */
export const cancelHabitCheckin = (id: string) => {
  return request<string>({
    url: 'shanzhu/habit/checkin/' + id,
    method: 'delete'
  })
}

/**
 * 查询习惯打卡记录
 * @param habitId
 * @param data
 */
export const queryHabitCheckins = (habitId: string, data: ShanzhuHabitCheckinQuery) => {
  return request<Array<ShanzhuHabitCheckinVO>>({
    url: 'shanzhu/habit/' + habitId + '/checkins',
    method: 'get',
    params: data
  })
}

import request from "@/utils/Request.ts";
import type {ShanzhuTodayWorkVO} from "@/api/shanzhu/todayWork/type/TodayWork.ts";

/**
 * 查询今日工作台数据
 */
export const queryTodayWork = () => {
  return request<ShanzhuTodayWorkVO>({
    url: 'shanzhu/today-work',
    method: 'get'
  })
}

import request from "@/utils/Request.ts";
import type {ShanzhuDashboard} from "@/api/shanzhu/dashboard/type/Dashboard.ts";

/**
 * 查询目标任务仪表盘统计
 */
export const queryDashboard = () => {
  return request<ShanzhuDashboard>({
    url: 'shanzhu/dashboard',
    method: 'get'
  })
}

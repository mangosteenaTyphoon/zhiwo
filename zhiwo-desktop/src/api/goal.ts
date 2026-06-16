import request from "./request";

export interface GoalVO {
  id: string;
  title: string;
  categoryId: string;
  categoryName: string;
  categoryColor: string;
  status: string;
  progress: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
}

/** 分页查询目标列表 */
export function getGoalPage(params?: { pageNum?: number; pageSize?: number; status?: string }): Promise<PageResult<GoalVO>> {
  return request({ url: "/shanzhu/goal/page", method: "post", data: { ...params, pageNum: params?.pageNum || 1, pageSize: params?.pageSize || 100 } });
}

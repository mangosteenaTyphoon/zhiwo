import request from "@/utils/Request.ts";
import type {ShanzhuReview, ShanzhuReviewQuery} from "@/api/shanzhu/review/type/Review.ts";

/**
 * 查询复盘分析报告
 * @param data
 */
export const queryReviewSummary = (data: ShanzhuReviewQuery) => {
  return request<ShanzhuReview>({
    url: 'shanzhu/review/summary',
    method: 'post',
    data: data
  })
}

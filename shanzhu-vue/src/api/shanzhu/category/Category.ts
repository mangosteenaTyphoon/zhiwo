import request from "@/utils/Request.ts";
import type {ShanzhuCategory} from "@/api/shanzhu/category/type/Category.ts";

/**
 * 查询分类列表
 */
export const queryCategoryList = () => {
  return request<Array<ShanzhuCategory>>({
    url: 'shanzhu/category/list',
    method: 'post'
  })
}

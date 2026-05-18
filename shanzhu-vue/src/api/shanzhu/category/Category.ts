import request from "@/utils/Request.ts";
import type {ShanzhuCategory, ShanzhuCategorySave} from "@/api/shanzhu/category/type/Category.ts";

/**
 * 查询分类列表
 */
export const queryCategoryList = () => {
  return request<Array<ShanzhuCategory>>({
    url: 'shanzhu/category/list',
    method: 'post'
  })
}

/**
 * 新增分类
 * @param data
 */
export const addCategory = (data: ShanzhuCategorySave) => {
  return request<string>({
    url: 'shanzhu/category/add',
    method: 'post',
    data: data
  })
}

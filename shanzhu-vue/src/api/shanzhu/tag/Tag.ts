import request from "@/utils/Request.ts";
import type {
  ShanzhuTag,
  ShanzhuTagQuery,
  ShanzhuTagRelationDelete,
  ShanzhuTagRelationQuery,
  ShanzhuTagRelationSave,
  ShanzhuTagSave
} from "@/api/shanzhu/tag/type/Tag.ts";

/**
 * 查询标签列表
 * @param data
 */
export const queryTagList = (data?: ShanzhuTagQuery) => {
  return request<Array<ShanzhuTag>>({
    url: 'shanzhu/tag/list',
    method: 'post',
    data: data
  })
}

/**
 * 搜索标签
 * @param data
 */
export const searchTag = (data?: ShanzhuTagQuery) => {
  return request<Array<ShanzhuTag>>({
    url: 'shanzhu/tag/search',
    method: 'post',
    data: data
  })
}

/**
 * 新增标签
 * @param data
 */
export const addTag = (data: ShanzhuTagSave) => {
  return request<string>({
    url: 'shanzhu/tag/add',
    method: 'post',
    data: data
  })
}

/**
 * 保存标签关系
 * @param data
 */
export const saveTagRelation = (data: ShanzhuTagRelationSave) => {
  return request<string>({
    url: 'shanzhu/tag/relation/save',
    method: 'post',
    data: data
  })
}

/**
 * 查询标签关系
 * @param data
 */
export const queryTagRelation = (data: ShanzhuTagRelationQuery) => {
  return request<Array<ShanzhuTag>>({
    url: 'shanzhu/tag/relation/list',
    method: 'post',
    data: data
  })
}

/**
 * 删除标签关系
 * @param data
 */
export const deleteTagRelation = (data: ShanzhuTagRelationDelete) => {
  return request<string>({
    url: 'shanzhu/tag/relation/delete',
    method: 'post',
    data: data
  })
}

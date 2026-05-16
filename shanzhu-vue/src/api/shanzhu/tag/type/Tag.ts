export interface ShanzhuTag {
  /**
   * 标签ID
   */
  id?: string;
  /**
   * 标签名称
   */
  name?: string;
  /**
   * 标签颜色
   */
  color?: string;
  /**
   * 标签类型
   */
  tagType?: string;
  /**
   * 使用次数
   */
  usageCount?: number;
  /**
   * 是否内置
   */
  builtIn?: string;
}

export interface ShanzhuTagQuery {
  /**
   * 搜索关键字
   */
  keyword?: string;
  /**
   * 标签类型
   */
  tagType?: string;
}

export interface ShanzhuTagSave {
  /**
   * 标签名称
   */
  name: string;
  /**
   * 标签颜色
   */
  color?: string;
  /**
   * 标签类型
   */
  tagType?: string;
}

export interface ShanzhuTagRelationQuery {
  /**
   * 业务类型
   */
  bizType: string;
  /**
   * 业务ID
   */
  bizId: string;
}

export interface ShanzhuTagRelationSave extends ShanzhuTagRelationQuery {
  /**
   * 标签ID列表
   */
  tagIds?: string[];
}

export interface ShanzhuTagRelationDelete extends ShanzhuTagRelationQuery {
  /**
   * 标签ID列表，为空时删除当前业务对象全部标签关系
   */
  tagIds?: string[];
}

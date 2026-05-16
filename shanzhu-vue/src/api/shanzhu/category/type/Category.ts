export interface ShanzhuCategory {
  /**
   * 分类ID
   */
  id?: string;
  /**
   * 分类名称
   */
  name?: string;
  /**
   * 分类编码
   */
  code?: string;
  /**
   * 图标
   */
  icon?: string;
  /**
   * 颜色
   */
  color?: string;
  /**
   * 排序
   */
  sortOrder?: number;
  /**
   * 是否内置
   */
  builtIn?: string;
  /**
   * 是否启用
   */
  enabled?: string;
}

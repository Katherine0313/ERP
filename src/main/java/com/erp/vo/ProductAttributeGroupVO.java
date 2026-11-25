package com.erp.vo;

import lombok.Data;

/**
 * 商品属性组列表项。
 */
@Data
public class ProductAttributeGroupVO {

    /** 主键 */
    private Long id;

    /** 属性组名 */
    private String name;

    /** 排序 */
    private String sort;

    /** 是否启用 */
    private Boolean enabled;

    /** 该组下的属性值数量 */
    private Integer valueCount;
}

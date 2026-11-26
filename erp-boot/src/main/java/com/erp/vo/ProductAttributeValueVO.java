package com.erp.vo;

import lombok.Data;

/**
 * 商品属性值列表项。
 */
@Data
public class ProductAttributeValueVO {

    /** 主键 */
    private Long id;

    /** 所属属性组id */
    private Long groupId;

    /** 所属属性组名（关联 product_attribute_group） */
    private String groupName;

    /** 属性值 */
    private String value;

    /** 排序 */
    private String sort;

    /** 是否启用 */
    private Boolean enabled;
}

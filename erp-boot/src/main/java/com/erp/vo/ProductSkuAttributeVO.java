package com.erp.vo;

import lombok.Data;

/**
 * SKU 的一个属性值（属于某个属性组）。
 */
@Data
public class ProductSkuAttributeVO {

    private Long groupId;

    /** 属性组名 */
    private String groupName;

    private Long valueId;

    /** 属性值 */
    private String valueName;
}

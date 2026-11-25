package com.erp.dto;

import lombok.Data;

/**
 * SKU 的一个可售单位。换算比例不存这里，统一取 product_unit.ratio。
 */
@Data
public class ProductSkuUnitDTO {

    /** 单位 id（product_unit.id） */
    private Long unitId;

    /** 是否默认单位：1是 0否 */
    private String defaultFlag;
}

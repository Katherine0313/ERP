package com.erp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 的一个可售单位。换算比例来自 product_unit，不重复存储。
 */
@Data
public class ProductSkuUnitVO {

    private Long id;

    private Long skuId;

    private Long unitId;

    /** 单位名称 */
    private String unitName;

    /** 换算成基础单位的比例（联表带出） */
    private BigDecimal ratio;

    /** 是否默认单位 */
    private String defaultFlag;
}

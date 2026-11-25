package com.erp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 的一条价格。
 */
@Data
public class ProductPriceVO {

    private Long id;

    private Long skuId;

    /** 计价单位 id，0 表示基础单位 */
    private Long unitId;

    /** 计价单位名称 */
    private String unitName;

    /** 价格类型 */
    private String priceType;

    /** 对象类型 */
    private String partnerType;

    /** 客户/供应商 id */
    private Long partnerId;

    /** 客户等级 */
    private String customerLevel;

    /** 价格 */
    private BigDecimal price;

    /** 税率(%) */
    private BigDecimal taxRate;

    /** 是否含税价 */
    private String includeTax;

    /** 是否启用 */
    private Boolean enabled;
}

package com.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 的一条价格。
 */
@Data
public class ProductPriceDTO {

    /** 计价单位 id，0 表示基础单位 */
    private Long unitId;

    /** 价格类型：1采购 2零售 3批发 4最低 5客户等级 6客户协议 7供应商 */
    private String priceType;

    /** 对象类型：0无 1客户 2供应商 */
    private String partnerType;

    /** 客户/供应商 id，0 表示不限 */
    private Long partnerId;

    /** 客户等级，空表示不限 */
    private String customerLevel;

    /** 价格 */
    private BigDecimal price;

    /** 税率(%) */
    private BigDecimal taxRate;

    /** 是否含税价：1含税 0不含税 */
    private String includeTax;

    /** 是否启用 */
    private Boolean enabled;
}

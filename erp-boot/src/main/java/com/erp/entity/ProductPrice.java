package com.erp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品价格，对应 product_price 表。
 *
 * <p>unitId=0 表示按基础单位计价；价格类型见 PRICE_TYPE 字典。
 */
@Data
public class ProductPrice {

    /** 主键 */
    private Long id;

    /** 产品 id（冗余便于查询） */
    private Long productId;

    /** SKU id */
    private Long skuId;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createdBy;

    /** 更新人 */
    private Long updatedBy;
}

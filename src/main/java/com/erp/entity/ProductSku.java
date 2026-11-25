package com.erp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品 SKU，对应 product_sku 表。
 *
 * <p>一行 = 一组属性值组合；价格、多单位、库存都挂在 SKU 维度。
 */
@Data
public class ProductSku {

    /** 主键 */
    private Long id;

    /** 产品 id */
    private Long productId;

    /** SKU 编码 */
    private String skuCode;

    /** 条码（全局唯一，允许空） */
    private String barCode;

    /** 属性值组合签名（MD5），无属性固定为 0 */
    private String attrSignature;

    /** 是否默认 SKU：1是 0否 */
    private String defaultFlag;

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

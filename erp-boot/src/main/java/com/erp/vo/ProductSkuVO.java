package com.erp.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情里的一个 SKU，内嵌属性值、价格、多单位。
 */
@Data
public class ProductSkuVO {

    private Long id;

    /** SKU 编码 */
    private String skuCode;

    /** 条码 */
    private String barCode;

    /** 属性值组合签名 */
    private String attrSignature;

    /** 是否默认 SKU */
    private String defaultFlag;

    /** 是否启用 */
    private Boolean enabled;

    /** 该 SKU 的属性值组合 */
    private List<ProductSkuAttributeVO> attributes = new ArrayList<>();

    /** 该 SKU 的价格 */
    private List<ProductPriceVO> prices = new ArrayList<>();

    /** 该 SKU 的多单位 */
    private List<ProductSkuUnitVO> units = new ArrayList<>();
}

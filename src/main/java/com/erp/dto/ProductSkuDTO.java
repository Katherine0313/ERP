package com.erp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存商品时的一个 SKU，价格和单位内嵌，避免依赖数据库生成的临时 id。
 */
@Data
public class ProductSkuDTO {

    /** SKU 编码 */
    private String skuCode;

    /** 条码 */
    private String barCode;

    /** 是否默认 SKU：1是 0否 */
    private String defaultFlag;

    /** 是否启用 */
    private Boolean enabled;

    /**
     * 属性值组合签名，由服务端重算后回填，前端传什么都会被覆盖。
     */
    private String attrSignature;

    /** 该 SKU 的属性值组合 */
    private List<ProductSkuAttributeDTO> attributes = new ArrayList<>();

    /** 该 SKU 的价格 */
    private List<ProductPriceDTO> prices = new ArrayList<>();

    /** 该 SKU 的多单位 */
    private List<ProductSkuUnitDTO> units = new ArrayList<>();
}

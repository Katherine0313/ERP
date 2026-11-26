package com.erp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品分页列表行。
 *
 * <p>categoryName / unitName 由联表带出；skuCount 为未删除 SKU 数；
 * 四个价格分别取基础单位（unit_id=0）下对应 price_type 的第一条启用价格。
 */
@Data
public class ProductListVO {

    private Long id;

    /** 商品编码 */
    private String productNo;

    /** 商品名称 */
    private String name;

    /** 助记码 */
    private String mnemonic;

    /** 类目 id / 名称 */
    private Long categoryId;
    private String categoryName;

    /** 默认单位 id / 名称 */
    private Long unitId;
    private String unitName;

    /** SKU 规格数 */
    private Integer skuCount;

    /** 库存数量（手动修正） */
    private BigDecimal stockQuantity;

    /** 成本价（手动修正） */
    private BigDecimal costPrice;

    /** 采购价（price_type=1） */
    private BigDecimal purchasePrice;

    /** 零售价（price_type=2） */
    private BigDecimal retailPrice;

    /** 销售价（price_type=3） */
    private BigDecimal salePrice;

    /** 最低售价（price_type=4） */
    private BigDecimal lowPrice;

    /** 状态：0草稿 1启用 2停售 3淘汰 */
    private String status;

    /** 是否开启序列号 / 批次 */
    private String enableSerialNumber;
    private String enableBatchNumber;

    /** 创建时间 */
    private LocalDateTime createTime;
}

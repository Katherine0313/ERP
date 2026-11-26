package com.erp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品（SPU），对应 product 表。
 *
 * <p>categoryName / unitName 是联表带出来的展示字段，不在 product 表里。
 */
@Data
public class Product {

    /** 主键 */
    private Long id;

    /** 商品编码，唯一 */
    private String productNo;

    /** 类目 id */
    private Long categoryId;

    /** 类目名称（关联 product_category） */
    private String categoryName;

    /** 商品名称 */
    private String name;

    /** 制造商 */
    private String mfrs;

    /** 品牌 */
    private String brand;

    /** 助记码 */
    private String mnemonic;

    /** 默认基础计量单位 id */
    private Long unitId;

    /** 默认单位名称（关联 product_unit） */
    private String unitName;

    /** 保质期天数 */
    private Integer expiryNum;

    /** 基础重量(kg) */
    private BigDecimal weight;

    /** 库存数量（手动修正；库存模块建成后改为聚合） */
    private BigDecimal stockQuantity;

    /** 成本价（手动修正） */
    private BigDecimal costPrice;

    /** 主图 */
    private String imgName;

    /** 备注 */
    private String remark;

    /** 状态：0草稿 1启用 2停售 3淘汰 */
    private String status;

    /** 是否开启序列号：0否 1是 */
    private String enableSerialNumber;

    /** 是否开启批次：0否 1是 */
    private String enableBatchNumber;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createdBy;

    /** 更新人 */
    private Long updatedBy;
}

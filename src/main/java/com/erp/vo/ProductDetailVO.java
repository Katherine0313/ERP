package com.erp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情：基本信息 + 启用的属性组 + SKU 列表（含价格与多单位）。
 */
@Data
public class ProductDetailVO {

    private Long id;

    /** 商品编码 */
    private String productNo;

    private Long categoryId;
    private String name;
    private String mfrs;
    private String brand;
    private String mnemonic;
    private Long unitId;
    private Integer expiryNum;
    private BigDecimal weight;
    private String imgName;
    private String remark;

    /** 状态：0草稿 1启用 2停售 3淘汰 */
    private String status;

    /** 是否开启序列号 / 批次 */
    private String enableSerialNumber;
    private String enableBatchNumber;

    /** 该商品启用的属性组 id（决定 SKU 的维度顺序） */
    private List<Long> attributeGroupIds = new ArrayList<>();

    /** SKU 列表 */
    private List<ProductSkuVO> skus = new ArrayList<>();
}

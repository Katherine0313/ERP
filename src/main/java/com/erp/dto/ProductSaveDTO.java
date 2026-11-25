package com.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品整体保存入参。
 *
 * <p>一次请求覆盖商品主表 + 属性组关联 + SKU + 价格 + 多单位，
 * 事务一致性由服务端保证；attr_signature 由服务端重算，前端传的不作数。
 */
@Data
public class ProductSaveDTO {

    /** 商品编码，唯一 */
    @NotBlank(message = "商品编码不能为空")
    private String productNo;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private Long categoryId;
    private Long unitId;
    private String mfrs;
    private String brand;
    private String mnemonic;
    private Integer expiryNum;
    private BigDecimal weight;
    private BigDecimal stockQuantity;
    private BigDecimal costPrice;
    private String imgName;
    private String remark;

    /** 状态：0草稿 1启用 2停售 3淘汰 */
    private String status;

    /** 是否开启序列号 / 批次：0否 1是 */
    private String enableSerialNumber;
    private String enableBatchNumber;

    /** 该商品启用的属性组 id */
    private List<Long> attributeGroupIds = new ArrayList<>();

    /** SKU（含属性组合、价格、多单位） */
    private List<ProductSkuDTO> skus = new ArrayList<>();
}

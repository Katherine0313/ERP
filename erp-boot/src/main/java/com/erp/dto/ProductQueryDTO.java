package com.erp.dto;

import lombok.Data;

/**
 * 商品分页查询条件。
 */
@Data
public class ProductQueryDTO {

    /** 商品名称 / 编码 / 助记码 模糊匹配 */
    private String keyword;

    /** 状态精确匹配 */
    private String status;

    /** 类目 id 精确匹配 */
    private Long categoryId;

    /** 页码，从 1 开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 20;
}

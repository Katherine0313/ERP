package com.erp.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品类目树节点。
 */
@Data
public class ProductCategoryVO {

    /** 主键 */
    private Long id;

    /** 上级类目id，0为顶级 */
    private Long parentId;

    /** 类目名称 */
    private String name;

    /** 类目编号 */
    private String serialNo;

    /** 显示顺序 */
    private String sort;

    /** 备注 */
    private String remark;

    /** 层级 */
    private Integer categoryLevel;

    /** 子节点 */
    private List<ProductCategoryVO> children = new ArrayList<>();
}

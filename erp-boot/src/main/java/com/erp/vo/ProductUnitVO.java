package com.erp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计量单位列表行，联表带出所属基础单位名。
 */
@Data
public class ProductUnitVO {

    private Long id;

    /** 单位名称 */
    private String name;

    /** 所属基础单位 id，0 表示基础单位 */
    private Long groupId;

    /** 所属基础单位名称（联表带出，基础单位自身为 -） */
    private String groupName;

    /** 换算成基础单位的比例 */
    private BigDecimal ratio;

    /** 是否基础单位：1是 0否 */
    private String isBasic;

    /** 排序 */
    private String sort;

    /** 是否启用 */
    private Boolean enabled;
}

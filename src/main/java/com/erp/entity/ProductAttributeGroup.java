package com.erp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品属性组，对应 product_attribute_group 表。
 *
 * <p>属性组是公共字典，可被多个商品复用（商品↔属性组多对多）。
 */
@Data
public class ProductAttributeGroup {

    /** 主键 */
    private Long id;

    /** 属性组名，如 颜色/型号/内存 */
    private String name;

    /** 排序 */
    private String sort;

    /** 是否启用 */
    private Boolean enabled;

    /** 软删除标记：0未删除，1删除 */
    private String deleteFlag;

    /** 删除令牌，正常为0，删除时写入唯一值 */
    private String deletedToken;

    /** 创建人 */
    private Long createdBy;

    /** 更新人 */
    private Long updatedBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

package com.erp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品属性值，对应 product_attribute_value 表。
 *
 * <p>属性组与属性值一对多：value.groupId → group.id。
 */
@Data
public class ProductAttributeValue {

    /** 主键 */
    private Long id;

    /** 所属属性组id */
    private Long groupId;

    /** 属性值，如 黑色/Pro/256G */
    private String value;

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

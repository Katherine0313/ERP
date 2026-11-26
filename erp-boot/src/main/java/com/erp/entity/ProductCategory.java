package com.erp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品类目，对应 product_category 表。
 *
 * <p>自关联树，parentId=0 表示顶级。同级类目名唯一。
 */
@Data
public class ProductCategory {

    /** 主键 */
    private Long id;

    /** 上级类目id，0为顶级 */
    private Long parentId;

    /** 类目名称 */
    private String name;

    /** 层级 */
    private Integer categoryLevel;

    /** 类目编号 */
    private String serialNo;

    /** 显示顺序 */
    private String sort;

    /** 备注 */
    private String remark;

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

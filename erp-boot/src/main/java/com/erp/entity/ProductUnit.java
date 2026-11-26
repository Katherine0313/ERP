package com.erp.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计量单位，对应 product_unit 表。
 *
 * <p>一行 = 一个具体单位。基础单位 group_id=0、ratio=1、is_basic=1；
 * 副单位指向所属基础单位，ratio 为换算成基础单位的比例。
 */
@Data
public class ProductUnit {

    /** 主键 */
    private Long id;

    /** 单位名称，如 个/箱/托 */
    private String name;

    /** 所属单位组（基础单位 id），0 表示自身是基础单位 */
    private Long groupId;

    /** 换算成基础单位的比例 */
    private BigDecimal ratio;

    /** 是否基础单位：1是 0否 */
    private String isBasic;

    /** 排序 */
    private String sort;

    /** 是否启用 */
    private Boolean enabled;

    /** 删除标记：0正常 1删除 */
    private String deleteFlag;

    /** 删除令牌 */
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

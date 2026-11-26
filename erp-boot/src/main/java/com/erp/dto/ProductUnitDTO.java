package com.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 计量单位新增/修改入参。
 *
 * <p>一行 = 一个单位：
 * 基础单位 groupId=0、ratio=1；副单位指向所属基础单位并填换算比例。
 */
@Data
public class ProductUnitDTO {

    /** 单位名称，如 个 / 箱 / 托 */
    @NotBlank(message = "单位名称不能为空")
    @Size(max = 50, message = "单位名称不能超过50个字符")
    private String name;

    /**
     * 所属基础单位 id，0 表示自身是基础单位。
     * isBasic=true 时后端强制写 0。
     */
    private Long groupId;

    /** 换算成基础单位的比例，基础单位固定 1 */
    private BigDecimal ratio;

    /** 是否基础单位 */
    private Boolean isBasic;

    /** 排序 */
    @Size(max = 10, message = "排序不能超过10个字符")
    private String sort;

    /** 是否启用 */
    private Boolean enabled;
}

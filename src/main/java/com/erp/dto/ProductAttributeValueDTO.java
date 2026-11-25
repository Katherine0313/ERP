package com.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品属性值新增/修改入参。
 */
@Data
public class ProductAttributeValueDTO {

    /** 所属属性组id */
    @NotNull(message = "所属属性组不能为空")
    private Long groupId;

    /** 属性值 */
    @NotBlank(message = "属性值不能为空")
    @Size(max = 100, message = "属性值不能超过100个字符")
    private String value;

    /** 排序 */
    @Size(max = 10, message = "排序不能超过10个字符")
    private String sort;

    /** 是否启用 */
    private Boolean enabled;
}

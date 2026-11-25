package com.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品属性组新增/修改入参。
 */
@Data
public class ProductAttributeGroupDTO {

    /** 属性组名 */
    @NotBlank(message = "属性组名不能为空")
    @Size(max = 50, message = "属性组名不能超过50个字符")
    private String name;

    /** 排序 */
    @Size(max = 10, message = "排序不能超过10个字符")
    private String sort;

    /** 是否启用 */
    private Boolean enabled;
}

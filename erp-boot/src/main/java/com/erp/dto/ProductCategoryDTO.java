package com.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品类目新增/修改入参。
 */
@Data
public class ProductCategoryDTO {

    /** 上级类目id，0为顶级 */
    private Long parentId;

    /** 类目名称 */
    @NotBlank(message = "类目名称不能为空")
    @Size(max = 50, message = "类目名称不能超过50个字符")
    private String name;

    /** 类目编号 */
    @Size(max = 100, message = "类目编号不能超过100个字符")
    private String serialNo;

    /** 显示顺序 */
    @Size(max = 10, message = "显示顺序不能超过10个字符")
    private String sort;

    /** 备注 */
    @Size(max = 1024, message = "备注不能超过1024个字符")
    private String remark;
}

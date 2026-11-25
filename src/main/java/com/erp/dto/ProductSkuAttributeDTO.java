package com.erp.dto;

import lombok.Data;

/**
 * SKU 的一个属性值，group_id 用于校验「每个属性组只能取一个值」。
 */
@Data
public class ProductSkuAttributeDTO {

    private Long groupId;

    private Long valueId;
}

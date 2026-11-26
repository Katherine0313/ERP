package com.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 计量单位批量启用/停用入参。
 */
@Data
public class ProductUnitBatchStatusDTO {

    private List<Long> ids;

    /** 目标状态：true 启用，false 停用 */
    private Boolean enabled;
}

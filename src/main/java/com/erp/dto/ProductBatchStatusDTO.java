package com.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量启用/禁用入参。
 */
@Data
public class ProductBatchStatusDTO {

    private List<Long> ids;

    /** 目标状态：1启用 2停售（禁用） */
    private String status;
}

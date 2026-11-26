package com.erp.dto;

import lombok.Data;

/**
 * 批量编辑入参。只传需要修改的字段，未传（null）的字段保持不变。
 */
@Data
public class ProductBatchEditDTO {

    private Long categoryId;

    private String brand;

    private String mfrs;

    private String mnemonic;

    /** 状态：0草稿 1启用 2停售 3淘汰 */
    private String status;

    private String remark;
}

package com.erp.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果。
 */
@Data
public class ProductImportResultVO {

    /** 总行数（不含表头） */
    private int total;

    /** 成功条数 */
    private int success;

    /** 失败条数 */
    private int fail;

    /** 失败原因（最多保留前 20 条，避免响应过大） */
    private List<String> messages = new ArrayList<>();
}

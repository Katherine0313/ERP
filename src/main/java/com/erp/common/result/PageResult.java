package com.erp.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页返回体。所有分页接口统一用它，前端只需认这一个结构。
 *
 * <p>分页参数约定：请求用 pageNum（从 1 开始）+ pageSize，
 * 后端 SQL 走手写原生分页（LIMIT offset, size），不引入分页插件。
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> of(Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }
}

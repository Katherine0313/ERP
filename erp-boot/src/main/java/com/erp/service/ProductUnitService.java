package com.erp.service;

import com.erp.common.result.PageResult;
import com.erp.dto.ProductUnitDTO;
import com.erp.entity.ProductUnit;
import com.erp.vo.ProductUnitVO;

import java.util.List;

/**
 * 计量单位服务。
 */
public interface ProductUnitService {

    /** 分页查询单位 */
    PageResult<ProductUnitVO> page(String keyword, Integer pageNum, Integer pageSize);

    /** 基础单位下拉 */
    List<ProductUnit> listBasic();

    /** 新增单位 */
    void create(ProductUnitDTO dto);

    /** 修改单位（类型不可改） */
    void update(Long id, ProductUnitDTO dto);

    /** 删除单位（有引用时拦截） */
    void delete(Long id);

    /** 启用/停用 */
    void updateStatus(Long id, Boolean enabled);

    /** 批量启用/停用 */
    void batchUpdateStatus(List<Long> ids, Boolean enabled);
}

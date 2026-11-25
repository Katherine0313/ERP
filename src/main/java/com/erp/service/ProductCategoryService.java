package com.erp.service;

import com.erp.dto.ProductCategoryDTO;
import com.erp.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品类目服务。
 */
public interface ProductCategoryService {

    /** 查询类目树 */
    List<ProductCategoryVO> tree();

    /** 查询类目平铺列表（下拉用） */
    List<ProductCategoryVO> list();

    /** 查询类目详情，查不到抛业务异常 */
    ProductCategoryVO detail(Long id);

    /** 新增类目 */
    void create(ProductCategoryDTO dto);

    /** 修改类目 */
    void update(Long id, ProductCategoryDTO dto);

    /** 删除类目（软删） */
    void delete(Long id);
}

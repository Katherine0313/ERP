package com.erp.service;

import com.erp.dto.ProductAttributeGroupDTO;
import com.erp.dto.ProductAttributeValueDTO;
import com.erp.common.result.PageResult;
import com.erp.vo.ProductAttributeGroupVO;
import com.erp.vo.ProductAttributeValueVO;

import java.util.List;

/**
 * 商品属性组 / 属性值服务。
 */
public interface ProductAttributeService {

    // ==================== 属性组 ====================

    /** 属性组分页 */
    PageResult<ProductAttributeGroupVO> groupPage(String keyword, Integer pageNum, Integer pageSize);

    /** 属性组下拉列表 */
    List<ProductAttributeGroupVO> groupList();

    /** 新增属性组 */
    void createGroup(ProductAttributeGroupDTO dto);

    /** 修改属性组 */
    void updateGroup(Long id, ProductAttributeGroupDTO dto);

    /** 删除属性组 */
    void deleteGroup(Long id);

    // ==================== 属性值 ====================

    /** 属性值分页 */
    PageResult<ProductAttributeValueVO> valuePage(Long groupId, String keyword, Integer pageNum, Integer pageSize);

    /** 新增属性值 */
    void createValue(ProductAttributeValueDTO dto);

    /** 修改属性值 */
    void updateValue(Long id, ProductAttributeValueDTO dto);

    /** 删除属性值 */
    void deleteValue(Long id);

    /** 启用/停用属性值 */
    void updateValueEnabled(Long id, Boolean enabled);
}

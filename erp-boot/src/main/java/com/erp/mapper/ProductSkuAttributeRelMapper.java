package com.erp.mapper;

import com.erp.vo.ProductSkuAttributeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * product_sku_attribute_rel（SKU-属性值）Mapper。
 */
@Mapper
public interface ProductSkuAttributeRelMapper {

    /** 查 SKU 的属性值组合（含属性组名、属性值） */
    List<ProductSkuAttributeVO> selectBySkuId(@Param("skuId") Long skuId);

    /** 新增关联 */
    int insert(@Param("skuId") Long skuId,
               @Param("productId") Long productId,
               @Param("groupId") Long groupId,
               @Param("valueId") Long valueId,
               @Param("createdBy") Long createdBy,
               @Param("createTime") LocalDateTime createTime);

    /** 物理删除商品下全部关联（保存时重建） */
    int physicalDeleteByProductId(@Param("productId") Long productId);
}

package com.erp.mapper;

import com.erp.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * product_sku Mapper。SKU 随商品保存整体重建，使用物理删除。
 */
@Mapper
public interface ProductSkuMapper {

    /** 查商品下未删除的 SKU */
    List<ProductSku> selectByProductId(@Param("productId") Long productId);

    /** 新增 SKU（回填自增 id） */
    int insert(ProductSku sku);

    /** 物理删除商品下全部 SKU（保存时重建） */
    int physicalDeleteByProductId(@Param("productId") Long productId);

    /** 统计商品下 SKU 数 */
    int countByProductId(@Param("productId") Long productId);
}

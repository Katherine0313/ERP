package com.erp.mapper;

import com.erp.vo.ProductSkuUnitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * product_sku_unit（SKU 多单位）Mapper。
 */
@Mapper
public interface ProductSkuUnitMapper {

    /** 查 SKU 的可售单位（含单位名、换算比例） */
    List<ProductSkuUnitVO> selectBySkuId(@Param("skuId") Long skuId);

    /** 新增关联 */
    int insert(@Param("skuId") Long skuId,
               @Param("productId") Long productId,
               @Param("unitId") Long unitId,
               @Param("defaultFlag") String defaultFlag,
               @Param("createdBy") Long createdBy,
               @Param("createTime") LocalDateTime createTime);

    /** 物理删除商品下全部关联（保存时重建） */
    int physicalDeleteByProductId(@Param("productId") Long productId);
}

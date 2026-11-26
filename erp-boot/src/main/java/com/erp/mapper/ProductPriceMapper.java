package com.erp.mapper;

import com.erp.entity.ProductPrice;
import com.erp.vo.ProductPriceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * product_price（SKU 价格）Mapper。
 */
@Mapper
public interface ProductPriceMapper {

    /** 查 SKU 的价格（含单位名） */
    List<ProductPriceVO> selectBySkuId(@Param("skuId") Long skuId);

    /** 新增价格（回填自增 id） */
    int insert(ProductPrice price);

    /** 物理删除商品下全部价格（保存时重建） */
    int physicalDeleteByProductId(@Param("productId") Long productId);

    /** 导入用：查默认 SKU 下某类型、基础单位的价格 id */
    Long selectIdForImport(@Param("skuId") Long skuId, @Param("priceType") String priceType);

    /** 导入用：只更新价格值 */
    int updatePriceValue(@Param("id") Long id,
                         @Param("price") BigDecimal price,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updateTime") LocalDateTime updateTime);
}

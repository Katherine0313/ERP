package com.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * product_attribute_rel（商品-属性组多对多）Mapper。
 */
@Mapper
public interface ProductAttributeRelMapper {

    /** 查商品启用的属性组 id（按 sort 排序） */
    List<Long> selectGroupIdsByProductId(@Param("productId") Long productId);

    /** 新增关联 */
    int insert(@Param("productId") Long productId,
               @Param("groupId") Long groupId,
               @Param("sort") String sort,
               @Param("createdBy") Long createdBy,
               @Param("createTime") LocalDateTime createTime);

    /** 物理删除商品下全部关联（保存时重建） */
    int physicalDeleteByProductId(@Param("productId") Long productId);
}

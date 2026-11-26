package com.erp.mapper;

import com.erp.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品类目 Mapper，SQL 写在 resources/mapper/ProductCategoryMapper.xml。
 */
@Mapper
public interface ProductCategoryMapper {

    /** 查全部未删除的类目（平铺，服务端组装成树） */
    List<ProductCategory> selectAll();

    /** 按 id 查未删除的类目，查不到返回 null */
    ProductCategory selectById(@Param("id") Long id);

    /** 按 id 查（含已删除），用于唯一性校验时排除自身 */
    ProductCategory selectByIdAny(@Param("id") Long id);

    /** 同级类目名是否已存在（排除自身 id），已删除的同名不算冲突 */
    int countByName(@Param("parentId") Long parentId,
                    @Param("name") String name,
                    @Param("excludeId") Long excludeId);

    /** 该类目下未删除的子类目数量 */
    int countChildren(@Param("id") Long id);

    /** 该类目下未删除的商品数量 */
    int countProducts(@Param("id") Long id);

    /** 新增类目 */
    int insert(ProductCategory category);

    /** 修改类目 */
    int update(ProductCategory category);

    /** 软删除：delete_flag=1，同时把 deleted_token 写成唯一值 */
    int softDelete(@Param("id") Long id,
                   @Param("deletedToken") String deletedToken,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updateTime") java.time.LocalDateTime updateTime);
}

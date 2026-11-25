package com.erp.mapper;

import com.erp.entity.ProductAttributeGroup;
import com.erp.entity.ProductAttributeValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品属性组 / 属性值 Mapper，SQL 写在 resources/mapper/ProductAttributeMapper.xml。
 *
 * <p>属性组与属性值结构高度相似（都是「名称 + 排序 + 启用 + 软删」），
 * 放在一个 Mapper 里，避免拆成两个几乎一样的文件。
 */
@Mapper
public interface ProductAttributeMapper {

    // ==================== 属性组 ====================

    /** 查属性组分页数据 */
    List<ProductAttributeGroup> selectGroupPage(@Param("keyword") String keyword,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);

    /** 查属性组总数 */
    long countGroupPage(@Param("keyword") String keyword);

    /** 查全部未删除的属性组（下拉用） */
    List<ProductAttributeGroup> selectGroupList();

    /** 按 id 查未删除的属性组 */
    ProductAttributeGroup selectGroupById(@Param("id") Long id);

    /** 属性组名是否重复（排除自身） */
    int countGroupByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /** 该属性组下的属性值数量 */
    int countGroupValues(@Param("groupId") Long groupId);

    /** 新增属性组 */
    int insertGroup(ProductAttributeGroup group);

    /** 修改属性组 */
    int updateGroup(ProductAttributeGroup group);

    /** 软删除属性组 */
    int softDeleteGroup(@Param("id") Long id,
                        @Param("deletedToken") String deletedToken,
                        @Param("updatedBy") Long updatedBy,
                        @Param("updateTime") LocalDateTime updateTime);

    // ==================== 属性值 ====================

    /** 查属性值分页数据 */
    List<ProductAttributeValue> selectValuePage(@Param("groupId") Long groupId,
                                                 @Param("keyword") String keyword,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);

    /** 查属性值总数 */
    long countValuePage(@Param("groupId") Long groupId, @Param("keyword") String keyword);

    /** 按 id 查未删除的属性值 */
    ProductAttributeValue selectValueById(@Param("id") Long id);

    /** 同一组下属性值是否重复（排除自身） */
    int countValueByValue(@Param("groupId") Long groupId,
                          @Param("value") String value,
                          @Param("excludeId") Long excludeId);

    /** 该属性值被多少个 SKU 引用 */
    int countSkuUsage(@Param("valueId") Long valueId);

    /** 新增属性值 */
    int insertValue(ProductAttributeValue value);

    /** 修改属性值 */
    int updateValue(ProductAttributeValue value);

    /** 软删除属性值 */
    int softDeleteValue(@Param("id") Long id,
                        @Param("deletedToken") String deletedToken,
                        @Param("updatedBy") Long updatedBy,
                        @Param("updateTime") LocalDateTime updateTime);

    /** 修改属性值启用状态 */
    int updateValueEnabled(@Param("id") Long id,
                           @Param("enabled") Boolean enabled,
                           @Param("updatedBy") Long updatedBy,
                           @Param("updateTime") LocalDateTime updateTime);
}

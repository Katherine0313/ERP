package com.erp.mapper;

import com.erp.entity.ProductUnit;
import com.erp.vo.ProductUnitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * product_unit（计量单位）Mapper，SQL 写在 resources/mapper/ProductUnitMapper.xml。
 */
@Mapper
public interface ProductUnitMapper {

    /** 查全部启用的单位（商品信息页下拉用，仍保留） */
    List<ProductUnit> selectAll();

    /** 分页查询单位（联表带出所属基础单位名） */
    List<ProductUnitVO> selectPage(@Param("keyword") String keyword,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /** 分页总数 */
    long countPage(@Param("keyword") String keyword);

    /** 查基础单位下拉（is_basic=1，isBasicOnly 用） */
    List<ProductUnit> selectBasicList();

    /** 按 id 查未删除单位 */
    ProductUnit selectById(@Param("id") Long id);

    /** 单位名是否重复（排除自身） */
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /** 有多少副单位挂在该基础单位下 */
    int countChildren(@Param("id") Long id);

    /** 该单位被商品默认单位引用的次数 */
    int countProductUsage(@Param("id") Long id);

    /** 该单位被 SKU 多单位引用的次数 */
    int countSkuUnitUsage(@Param("id") Long id);

    /** 该单位被价格引用的次数 */
    int countPriceUsage(@Param("id") Long id);

    /** 新增单位（回填自增 id） */
    int insert(ProductUnit unit);

    /** 修改单位（类型与所属组不可改，只改名称/比例/排序/状态） */
    int update(ProductUnit unit);

    /** 启用/停用 */
    int updateEnabled(@Param("id") Long id,
                      @Param("enabled") Boolean enabled,
                      @Param("updatedBy") Long updatedBy,
                      @Param("updateTime") LocalDateTime updateTime);

    /** 批量启用/停用 */
    int batchUpdateEnabled(@Param("ids") List<Long> ids,
                           @Param("enabled") Boolean enabled,
                           @Param("updatedBy") Long updatedBy,
                           @Param("updateTime") LocalDateTime updateTime);

    /** 软删除 */
    int softDelete(@Param("id") Long id,
                   @Param("deletedToken") String deletedToken,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updateTime") LocalDateTime updateTime);
}

package com.erp.mapper;

import com.erp.dto.ProductBatchEditDTO;
import com.erp.dto.ProductQueryDTO;
import com.erp.entity.Product;
import com.erp.vo.ProductListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品主表 Mapper，SQL 写在 resources/mapper/ProductMapper.xml。
 */
@Mapper
public interface ProductMapper {

    /** 分页查询商品（含类目名/单位名/规格数/采购价/零售价/销售价/最低售价） */
    List<ProductListVO> selectPage(@Param("query") ProductQueryDTO query,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /** 分页查询总数 */
    long countPage(@Param("query") ProductQueryDTO query);

    /** 商品平铺下拉（未删除） */
    List<ProductListVO> selectOptions();

    /** 查全部未删除商品（兼容旧列表接口） */
    List<Product> selectAllProducts();

    /** 导出用：按查询条件取商品数据（最多 10000 条） */
    List<ProductListVO> selectForExport(@Param("query") ProductQueryDTO query);

    /** 按 id 查未删除商品 */
    Product selectById(@Param("id") Long id);

    /** 商品编码是否重复（排除自身；excludeId 为 null 表示新增） */
    int countByProductNo(@Param("productNo") String productNo, @Param("excludeId") Long excludeId);

    /** 新增商品（回填自增 id） */
    int insert(Product product);

    /** 修改商品基本信息 */
    int update(Product product);

    /** 修改上下架状态 */
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updatedBy") Long updatedBy,
                     @Param("updateTime") LocalDateTime updateTime);

    /** 软删除商品 */
    int softDelete(@Param("id") Long id,
                   @Param("deletedToken") String deletedToken,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updateTime") LocalDateTime updateTime);

    /** 批量改状态 */
    int batchUpdateStatus(@Param("ids") List<Long> ids,
                          @Param("status") String status,
                          @Param("updatedBy") Long updatedBy,
                          @Param("updateTime") LocalDateTime updateTime);

    /** 批量编辑（只更新 dto 中非 null 的字段） */
    int batchEdit(@Param("ids") List<Long> ids,
                  @Param("dto") ProductBatchEditDTO dto,
                  @Param("updatedBy") Long updatedBy,
                  @Param("updateTime") LocalDateTime updateTime);

    /** 批量修正库存 */
    int batchUpdateStock(@Param("ids") List<Long> ids,
                         @Param("quantity") BigDecimal quantity,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updateTime") LocalDateTime updateTime);

    /** 批量修正成本 */
    int batchUpdateCost(@Param("ids") List<Long> ids,
                        @Param("cost") BigDecimal cost,
                        @Param("updatedBy") Long updatedBy,
                        @Param("updateTime") LocalDateTime updateTime);

    /** 按编码查未删除商品（导入用） */
    Product selectByProductNo(@Param("productNo") String productNo);

    /** 按名称查类目 id（导入用，找不到返回 null） */
    Long selectCategoryIdByName(@Param("name") String name);

    /** 按名称查单位 id（导入用，找不到返回 null） */
    Long selectUnitIdByName(@Param("name") String name);
}

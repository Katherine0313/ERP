package com.erp.service;

import com.erp.common.result.PageResult;
import com.erp.dto.ProductBatchEditDTO;
import com.erp.dto.ProductQueryDTO;
import com.erp.dto.ProductSaveDTO;
import com.erp.entity.Product;
import com.erp.entity.ProductUnit;
import com.erp.vo.ProductDetailVO;
import com.erp.vo.ProductImportResultVO;
import com.erp.vo.ProductListVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品（SPU）服务。
 */
public interface ProductService {

    /** 分页查询商品 */
    PageResult<ProductListVO> page(ProductQueryDTO query);

    /** 商品详情（属性组 + SKU + 价格 + 多单位） */
    ProductDetailVO detail(Long id);

    /** 商品平铺下拉 */
    List<ProductListVO> listOptions();

    /** 按当前查询条件导出商品信息 Excel（返回文件字节） */
    byte[] exportExcel(ProductQueryDTO query);

    /** 单位下拉（供商品弹窗使用） */
    List<ProductUnit> listUnits();

    /** 新增商品（聚合保存） */
    void create(ProductSaveDTO dto);

    /** 修改商品（从属数据整体覆盖） */
    void update(Long id, ProductSaveDTO dto);

    /** 软删除商品 */
    void delete(Long id);

    /** 上下架 */
    void updateStatus(Long id, String status);

    /** 批量改状态 */
    void batchUpdateStatus(List<Long> ids, String status);

    /** 批量编辑（只更新传入的非空字段） */
    void batchEdit(List<Long> ids, ProductBatchEditDTO dto);

    /** 批量修正库存 */
    void batchUpdateStock(List<Long> ids, BigDecimal quantity);

    /** 批量修正成本 */
    void batchUpdateCost(List<Long> ids, BigDecimal cost);

    /** Excel 导入商品 */
    ProductImportResultVO importExcel(MultipartFile file);

    /** 导入模板 Excel */
    byte[] importTemplate();

    /** 兼容旧接口：返回未删除商品列表 */
    List<Product> list();
}

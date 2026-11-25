package com.erp.controller;

import com.erp.common.result.PageResult;
import com.erp.common.result.Result;
import com.erp.dto.ProductBatchEditDTO;
import com.erp.dto.ProductBatchStatusDTO;
import com.erp.dto.ProductQueryDTO;
import com.erp.dto.ProductSaveDTO;
import com.erp.entity.Product;
import com.erp.entity.ProductUnit;
import com.erp.service.ProductService;
import com.erp.vo.ProductDetailVO;
import com.erp.vo.ProductImportResultVO;
import com.erp.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 商品信息接口，管理端走 /api/admin/product。
 */
@Tag(name = "商品管理-商品信息", description = "商品分页、详情与聚合保存")
@RequestMapping("/api/admin/product")
@RestController
public class ProductController {

    @Resource
    private ProductService productService;

    @Operation(summary = "商品分页列表")
    @GetMapping("/page")
    public Result<PageResult<ProductListVO>> page(ProductQueryDTO query) {
        return Result.success(productService.page(query));
    }

    @Operation(summary = "导出商品信息 Excel")
    @GetMapping(value = "/export",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> export(ProductQueryDTO query) {
        byte[] bytes = productService.exportExcel(query);
        String filename = URLEncoder.encode("商品信息.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @Operation(summary = "批量启用/禁用")
    @PutMapping("/batch-status")
    public Result<Void> batchStatus(@RequestBody ProductBatchStatusDTO dto) {
        productService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "批量编辑")
    @PutMapping("/batch-edit")
    public Result<Void> batchEdit(@RequestParam List<Long> ids, @RequestBody ProductBatchEditDTO dto) {
        productService.batchEdit(ids, dto);
        return Result.success();
    }

    @Operation(summary = "批量修正库存")
    @PutMapping("/batch-stock")
    public Result<Void> batchStock(@RequestParam List<Long> ids, @RequestParam BigDecimal quantity) {
        productService.batchUpdateStock(ids, quantity);
        return Result.success();
    }

    @Operation(summary = "批量修正成本")
    @PutMapping("/batch-cost")
    public Result<Void> batchCost(@RequestParam List<Long> ids, @RequestParam BigDecimal cost) {
        productService.batchUpdateCost(ids, cost);
        return Result.success();
    }

    @Operation(summary = "导入商品 Excel")
    @PostMapping("/import")
    public Result<ProductImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(productService.importExcel(file));
    }

    @Operation(summary = "下载商品导入模板")
    @GetMapping(value = "/import/template",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> importTemplate() {
        String filename = URLEncoder.encode("商品导入模板.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(productService.importTemplate());
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @Operation(summary = "商品平铺下拉")
    @GetMapping("/list")
    public Result<List<ProductListVO>> listOptions() {
        return Result.success(productService.listOptions());
    }

    @Operation(summary = "计量单位下拉")
    @GetMapping("/unit/list")
    public Result<List<ProductUnit>> listUnits() {
        return Result.success(productService.listUnits());
    }

    @Operation(summary = "新增商品（含属性组/SKU/价格/多单位）")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductSaveDTO dto) {
        productService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改商品（从属数据整体覆盖）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductSaveDTO dto) {
        productService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @Operation(summary = "商品上下架")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "商品列表（兼容旧接口）")
    @GetMapping
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }
}

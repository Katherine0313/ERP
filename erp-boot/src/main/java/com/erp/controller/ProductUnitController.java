package com.erp.controller;

import com.erp.common.result.PageResult;
import com.erp.common.result.Result;
import com.erp.dto.ProductUnitBatchStatusDTO;
import com.erp.dto.ProductUnitDTO;
import com.erp.entity.ProductUnit;
import com.erp.service.ProductUnitService;
import com.erp.vo.ProductUnitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 计量单位接口，管理端走 /api/admin/product/unit。
 *
 * <p>商品信息页的单位下拉仍走 ProductController 的 /api/admin/product/unit/list。
 */
@Tag(name = "商品管理-多单位", description = "计量单位维护")
@RequestMapping("/api/admin/product/unit")
@RestController
public class ProductUnitController {

    @Resource
    private ProductUnitService productUnitService;

    @Operation(summary = "分页查询计量单位")
    @GetMapping("/page")
    public Result<PageResult<ProductUnitVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return Result.success(productUnitService.page(keyword, pageNum, pageSize));
    }

    @Operation(summary = "查询基础单位下拉")
    @GetMapping("/basic")
    public Result<List<ProductUnit>> basicList() {
        return Result.success(productUnitService.listBasic());
    }

    @Operation(summary = "新增计量单位")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductUnitDTO dto) {
        productUnitService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改计量单位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductUnitDTO dto) {
        productUnitService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除计量单位（有引用时拦截）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productUnitService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用/停用计量单位")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        productUnitService.updateStatus(id, enabled);
        return Result.success();
    }

    @Operation(summary = "批量启用/停用计量单位")
    @PutMapping("/batch-status")
    public Result<Void> batchStatus(@RequestBody ProductUnitBatchStatusDTO dto) {
        productUnitService.batchUpdateStatus(dto.getIds(), dto.getEnabled());
        return Result.success();
    }
}

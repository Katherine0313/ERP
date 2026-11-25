package com.erp.controller;

import com.erp.common.result.Result;
import com.erp.dto.ProductCategoryDTO;
import com.erp.service.ProductCategoryService;
import com.erp.vo.ProductCategoryVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品类目接口，管理端走 /api/admin/product/category。
 */
@Tag(name = "商品管理-商品类别", description = "商品类目树维护")
@RequestMapping("/api/admin/product/category")
@RestController
public class ProductCategoryController {

    @Resource
    private ProductCategoryService productCategoryService;

    @Operation(summary = "查询商品类目树")
    @GetMapping("/tree")
    public Result<List<ProductCategoryVO>> tree() {
        return Result.success(productCategoryService.tree());
    }

    @Operation(summary = "查询商品类目平铺列表")
    @GetMapping("/list")
    public Result<List<ProductCategoryVO>> list() {
        return Result.success(productCategoryService.list());
    }

    @Operation(summary = "查询商品类目详情")
    @GetMapping("/{id}")
    public Result<ProductCategoryVO> detail(@PathVariable Long id) {
        return Result.success(productCategoryService.detail(id));
    }

    @Operation(summary = "新增商品类目")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductCategoryDTO dto) {
        productCategoryService.create(dto);
        return Result.success();
    }

    @Operation(summary = "修改商品类目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductCategoryDTO dto) {
        productCategoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品类目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productCategoryService.delete(id);
        return Result.success();
    }
}

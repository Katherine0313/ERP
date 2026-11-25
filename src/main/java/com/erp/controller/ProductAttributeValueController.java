package com.erp.controller;

import com.erp.common.result.PageResult;
import com.erp.common.result.Result;
import com.erp.dto.ProductAttributeValueDTO;
import com.erp.service.ProductAttributeService;
import com.erp.vo.ProductAttributeValueVO;
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

/**
 * 商品属性值接口，管理端走 /api/admin/product/attribute-value。
 */
@Tag(name = "商品管理-多属性", description = "商品属性值维护")
@RequestMapping("/api/admin/product/attribute-value")
@RestController
public class ProductAttributeValueController {

    @Resource
    private ProductAttributeService productAttributeService;

    @Operation(summary = "分页查询商品属性值")
    @GetMapping("/page")
    public Result<PageResult<ProductAttributeValueVO>> page(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return Result.success(productAttributeService.valuePage(groupId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "新增商品属性值")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductAttributeValueDTO dto) {
        productAttributeService.createValue(dto);
        return Result.success();
    }

    @Operation(summary = "修改商品属性值")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductAttributeValueDTO dto) {
        productAttributeService.updateValue(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品属性值")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productAttributeService.deleteValue(id);
        return Result.success();
    }

    @Operation(summary = "启用或停用商品属性值")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        productAttributeService.updateValueEnabled(id, enabled);
        return Result.success();
    }
}

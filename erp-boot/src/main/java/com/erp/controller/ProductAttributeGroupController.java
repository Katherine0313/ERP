package com.erp.controller;

import com.erp.common.result.PageResult;
import com.erp.common.result.Result;
import com.erp.dto.ProductAttributeGroupDTO;
import com.erp.dto.ProductAttributeValueDTO;
import com.erp.service.ProductAttributeService;
import com.erp.vo.ProductAttributeGroupVO;
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

import java.util.List;

/**
 * 商品属性组接口，管理端走 /api/admin/product/attribute-group。
 */
@Tag(name = "商品管理-商品属性", description = "商品属性组维护")
@RequestMapping("/api/admin/product/attribute-group")
@RestController
public class ProductAttributeGroupController {

    @Resource
    private ProductAttributeService productAttributeService;

    @Operation(summary = "分页查询商品属性组")
    @GetMapping("/page")
    public Result<PageResult<ProductAttributeGroupVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return Result.success(productAttributeService.groupPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "查询商品属性组下拉列表")
    @GetMapping("/list")
    public Result<List<ProductAttributeGroupVO>> list() {
        return Result.success(productAttributeService.groupList());
    }

    @Operation(summary = "新增商品属性组")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductAttributeGroupDTO dto) {
        productAttributeService.createGroup(dto);
        return Result.success();
    }

    @Operation(summary = "修改商品属性组")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductAttributeGroupDTO dto) {
        productAttributeService.updateGroup(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品属性组")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productAttributeService.deleteGroup(id);
        return Result.success();
    }
}

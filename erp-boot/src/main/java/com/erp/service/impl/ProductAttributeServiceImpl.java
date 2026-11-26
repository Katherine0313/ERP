package com.erp.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.common.result.PageResult;
import com.erp.dto.ProductAttributeGroupDTO;
import com.erp.dto.ProductAttributeValueDTO;
import com.erp.entity.ProductAttributeGroup;
import com.erp.entity.ProductAttributeValue;
import com.erp.mapper.ProductAttributeMapper;
import com.erp.service.ProductAttributeService;
import com.erp.vo.ProductAttributeGroupVO;
import com.erp.vo.ProductAttributeValueVO;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 商品属性组 / 属性值维护。
 *
 * <p>属性组是「属性的名」，属性值是「属性的值」，两者分表维护。
 * 属性组被商品复用，所以删除属性组前必须先清掉组下的属性值。
 */
@Service
public class ProductAttributeServiceImpl implements ProductAttributeService {

    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Resource
    private ProductAttributeMapper productAttributeMapper;

    // ==================== 属性组 ====================

    @Override
    public PageResult<ProductAttributeGroupVO> groupPage(String keyword, Integer pageNum, Integer pageSize) {
        int size = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        int num = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int offset = (num - 1) * size;

        List<ProductAttributeGroup> groups = productAttributeMapper.selectGroupPage(keyword, offset, size);
        long total = productAttributeMapper.countGroupPage(keyword);

        List<ProductAttributeGroupVO> records = new ArrayList<>();
        for (ProductAttributeGroup group : groups) {
            records.add(toGroupVO(group));
        }
        return PageResult.of(total, records);
    }

    @Override
    public List<ProductAttributeGroupVO> groupList() {
        List<ProductAttributeGroup> groups = productAttributeMapper.selectGroupList();
        List<ProductAttributeGroupVO> records = new ArrayList<>();
        for (ProductAttributeGroup group : groups) {
            records.add(toGroupVO(group));
        }
        return records;
    }

    @Override
    public void createGroup(ProductAttributeGroupDTO dto) {
        String name = dto.getName().trim();
        if (productAttributeMapper.countGroupByName(name, null) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "属性组名已存在");
        }

        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setName(name);
        group.setSort(dto.getSort());
        group.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        group.setCreatedBy(currentUserId());
        group.setUpdatedBy(currentUserId());
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        productAttributeMapper.insertGroup(group);
    }

    @Override
    public void updateGroup(Long id, ProductAttributeGroupDTO dto) {
        ProductAttributeGroup existing = productAttributeMapper.selectGroupById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "属性组不存在或已删除");
        }

        String name = dto.getName().trim();
        if (productAttributeMapper.countGroupByName(name, id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "属性组名已存在");
        }

        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(id);
        group.setName(name);
        group.setSort(dto.getSort());
        group.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        group.setUpdatedBy(currentUserId());
        group.setUpdateTime(LocalDateTime.now());
        productAttributeMapper.updateGroup(group);
    }

    @Override
    public void deleteGroup(Long id) {
        ProductAttributeGroup existing = productAttributeMapper.selectGroupById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "属性组不存在或已删除");
        }
        // 组下还有属性值，先清掉再删组
        if (productAttributeMapper.countGroupValues(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先删除该属性组下的属性值");
        }
        productAttributeMapper.softDeleteGroup(id,
                UUID.randomUUID().toString().replace("-", ""),
                currentUserId(),
                LocalDateTime.now());
    }

    // ==================== 属性值 ====================

    @Override
    public PageResult<ProductAttributeValueVO> valuePage(Long groupId, String keyword, Integer pageNum, Integer pageSize) {
        int size = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        int num = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int offset = (num - 1) * size;

        List<ProductAttributeValue> values = productAttributeMapper.selectValuePage(groupId, keyword, offset, size);
        long total = productAttributeMapper.countValuePage(groupId, keyword);

        List<ProductAttributeValueVO> records = new ArrayList<>();
        for (ProductAttributeValue value : values) {
            records.add(toValueVO(value));
        }
        return PageResult.of(total, records);
    }

    @Override
    public void createValue(ProductAttributeValueDTO dto) {
        // 1. 属性组必须存在且启用
        ProductAttributeGroup group = productAttributeMapper.selectGroupById(dto.getGroupId());
        if (group == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属属性组不存在");
        }

        // 2. 同组下值不能重复
        String value = dto.getValue().trim();
        if (productAttributeMapper.countValueByValue(dto.getGroupId(), value, null) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该属性组下已存在同名属性值");
        }

        ProductAttributeValue entity = new ProductAttributeValue();
        entity.setGroupId(dto.getGroupId());
        entity.setValue(value);
        entity.setSort(dto.getSort());
        entity.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        entity.setCreatedBy(currentUserId());
        entity.setUpdatedBy(currentUserId());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        productAttributeMapper.insertValue(entity);
    }

    @Override
    public void updateValue(Long id, ProductAttributeValueDTO dto) {
        ProductAttributeValue existing = productAttributeMapper.selectValueById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "属性值不存在或已删除");
        }

        ProductAttributeGroup group = productAttributeMapper.selectGroupById(dto.getGroupId());
        if (group == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "所属属性组不存在");
        }

        String value = dto.getValue().trim();
        if (productAttributeMapper.countValueByValue(dto.getGroupId(), value, id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该属性组下已存在同名属性值");
        }

        ProductAttributeValue entity = new ProductAttributeValue();
        entity.setId(id);
        entity.setGroupId(dto.getGroupId());
        entity.setValue(value);
        entity.setSort(dto.getSort());
        entity.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        entity.setUpdatedBy(currentUserId());
        entity.setUpdateTime(LocalDateTime.now());
        productAttributeMapper.updateValue(entity);
    }

    @Override
    public void deleteValue(Long id) {
        ProductAttributeValue existing = productAttributeMapper.selectValueById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "属性值不存在或已删除");
        }
        // 属性值已被商品规格引用，不能删
        if (productAttributeMapper.countSkuUsage(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该属性值已被商品规格引用，无法删除");
        }
        productAttributeMapper.softDeleteValue(id,
                UUID.randomUUID().toString().replace("-", ""),
                currentUserId(),
                LocalDateTime.now());
    }

    @Override
    public void updateValueEnabled(Long id, Boolean enabled) {
        ProductAttributeValue existing = productAttributeMapper.selectValueById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "属性值不存在或已删除");
        }
        productAttributeMapper.updateValueEnabled(id,
                enabled == null ? Boolean.TRUE : enabled,
                currentUserId(),
                LocalDateTime.now());
    }

    // ==================== 转换 ====================

    /** 实体转 VO */
    private ProductAttributeGroupVO toGroupVO(ProductAttributeGroup group) {
        ProductAttributeGroupVO vo = new ProductAttributeGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setSort(group.getSort());
        vo.setEnabled(group.getEnabled());
        vo.setValueCount(productAttributeMapper.countGroupValues(group.getId()));
        return vo;
    }

    /** 实体转 VO */
    private ProductAttributeValueVO toValueVO(ProductAttributeValue value) {
        ProductAttributeValueVO vo = new ProductAttributeValueVO();
        vo.setId(value.getId());
        vo.setGroupId(value.getGroupId());
        vo.setValue(value.getValue());
        vo.setSort(value.getSort());
        vo.setEnabled(value.getEnabled());

        // 带出属性组名，方便列表直接展示
        ProductAttributeGroup group = productAttributeMapper.selectGroupById(value.getGroupId());
        vo.setGroupName(group == null ? "-" : group.getName());
        return vo;
    }

    /**
     * 取当前登录人 id。JWT 鉴权未接入时先返回 null，不阻塞功能。
     */
    private Long currentUserId() {
        return null;
    }
}

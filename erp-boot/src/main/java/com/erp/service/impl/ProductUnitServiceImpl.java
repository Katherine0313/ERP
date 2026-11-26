package com.erp.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.common.result.PageResult;
import com.erp.dto.ProductUnitDTO;
import com.erp.entity.ProductUnit;
import com.erp.mapper.ProductUnitMapper;
import com.erp.service.ProductUnitService;
import com.erp.vo.ProductUnitVO;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 计量单位维护。
 *
 * <p>一行 = 一个单位。基础单位 group_id=0、ratio=1；副单位指向基础单位并带换算比例。
 * 已被引用的单位不允许删除（商品默认单位 / SKU 多单位 / 价格）。
 */
@Service
public class ProductUnitServiceImpl implements ProductUnitService {

    @Resource
    private ProductUnitMapper productUnitMapper;

    @Override
    public PageResult<ProductUnitVO> page(String keyword, Integer pageNum, Integer pageSize) {
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;
        if (size > 100) {
            size = 100;
        }
        int offset = (current - 1) * size;
        long total = productUnitMapper.countPage(keyword);
        List<ProductUnitVO> records = productUnitMapper.selectPage(keyword, offset, size);
        return PageResult.of(total, records);
    }

    @Override
    public List<ProductUnit> listBasic() {
        return productUnitMapper.selectBasicList();
    }

    @Override
    public void create(ProductUnitDTO dto) {
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "单位名称不能为空");
        }
        if (productUnitMapper.countByName(name, null) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "单位名称已存在");
        }

        boolean basic = Boolean.TRUE.equals(dto.getIsBasic());
        Long groupId = 0L;
        BigDecimal ratio = BigDecimal.ONE;
        String isBasic = "0";

        if (basic) {
            isBasic = "1";
        } else {
            if (dto.getGroupId() == null || dto.getGroupId() <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "副单位必须选择所属基础单位");
            }
            ProductUnit base = productUnitMapper.selectById(dto.getGroupId());
            if (base == null || !"1".equals(base.getIsBasic())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "所属基础单位不存在");
            }
            groupId = dto.getGroupId();
            ratio = dto.getRatio();
            if (ratio == null || ratio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "换算比例必须大于0");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        ProductUnit unit = new ProductUnit();
        unit.setName(name);
        unit.setGroupId(groupId);
        unit.setRatio(ratio);
        unit.setIsBasic(isBasic);
        unit.setSort(dto.getSort());
        unit.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        unit.setCreatedBy(currentUserId());
        unit.setUpdatedBy(currentUserId());
        unit.setCreateTime(now);
        unit.setUpdateTime(now);
        productUnitMapper.insert(unit);
    }

    @Override
    public void update(Long id, ProductUnitDTO dto) {
        ProductUnit existing = productUnitMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "单位不存在或已删除");
        }
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "单位名称不能为空");
        }
        if (productUnitMapper.countByName(name, id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "单位名称已存在");
        }

        // 类型（is_basic / group_id）不允许修改，只更新名称/比例/排序/状态
        BigDecimal ratio = existing.getRatio();
        if (!"1".equals(existing.getIsBasic())) {
            ratio = dto.getRatio();
            if (ratio == null || ratio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "换算比例必须大于0");
            }
        }

        ProductUnit unit = new ProductUnit();
        unit.setId(id);
        unit.setName(name);
        unit.setRatio(ratio);
        unit.setSort(dto.getSort());
        unit.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        unit.setUpdatedBy(currentUserId());
        unit.setUpdateTime(LocalDateTime.now());
        productUnitMapper.update(unit);
    }

    @Override
    public void delete(Long id) {
        ProductUnit existing = productUnitMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "单位不存在或已删除");
        }
        if ("1".equals(existing.getIsBasic()) && productUnitMapper.countChildren(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该基础单位下还有副单位，无法删除");
        }
        if (productUnitMapper.countProductUsage(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已有商品使用该单位，无法删除");
        }
        if (productUnitMapper.countSkuUnitUsage(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已有 SKU 使用该单位，无法删除");
        }
        if (productUnitMapper.countPriceUsage(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已有价格使用该单位，无法删除");
        }
        productUnitMapper.softDelete(id,
                UUID.randomUUID().toString().replace("-", ""),
                currentUserId(), LocalDateTime.now());
    }

    @Override
    public void updateStatus(Long id, Boolean enabled) {
        ProductUnit existing = productUnitMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "单位不存在或已删除");
        }
        productUnitMapper.updateEnabled(id, enabled, currentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, Boolean enabled) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先选择要操作的单位");
        }
        if (enabled == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "状态不能为空");
        }
        productUnitMapper.batchUpdateEnabled(ids, enabled, currentUserId(), LocalDateTime.now());
    }

    /** 取当前登录人 id。JWT 上下文未接入时先返回 null，不阻塞功能。 */
    private Long currentUserId() {
        return null;
    }
}

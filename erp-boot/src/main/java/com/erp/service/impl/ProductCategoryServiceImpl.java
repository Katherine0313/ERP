package com.erp.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.dto.ProductCategoryDTO;
import com.erp.entity.ProductCategory;
import com.erp.mapper.ProductCategoryMapper;
import com.erp.service.ProductCategoryService;
import com.erp.vo.ProductCategoryVO;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 商品类目维护。
 *
 * <p>类目是一棵树，保存时按需更新子节点的层级。
 */
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public List<ProductCategoryVO> tree() {
        List<ProductCategory> all = productCategoryMapper.selectAll();
        return buildTree(all);
    }

    @Override
    public List<ProductCategoryVO> list() {
        List<ProductCategory> all = productCategoryMapper.selectAll();
        List<ProductCategoryVO> result = new ArrayList<>();
        for (ProductCategory category : all) {
            result.add(toVO(category));
        }
        return result;
    }

    @Override
    public ProductCategoryVO detail(Long id) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "类目不存在或已删除");
        }
        return toVO(category);
    }

    @Override
    public void create(ProductCategoryDTO dto) {
        // 1. 同级同名校验
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        String name = dto.getName().trim();
        if (productCategoryMapper.countByName(parentId, name, null) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "同级下已存在同名类目");
        }

        // 2. 上级类目必须存在，且不能挂到自己的子孙下（新建时没有子孙）
        int level = 1;
        if (parentId != 0L) {
            ProductCategory parent = productCategoryMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "上级类目不存在");
            }
            level = (parent.getCategoryLevel() == null ? 1 : parent.getCategoryLevel()) + 1;
        }

        // 3. 入库
        ProductCategory category = new ProductCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setCategoryLevel(level);
        category.setSerialNo(dto.getSerialNo());
        category.setSort(dto.getSort());
        category.setRemark(dto.getRemark());
        category.setCreatedBy(currentUserId());
        category.setUpdatedBy(currentUserId());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        productCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, ProductCategoryDTO dto) {
        ProductCategory existing = productCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "类目不存在或已删除");
        }

        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        String name = dto.getName().trim();

        // 1. 不能把自己挂到自己下面
        if (parentId == id) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "上级类目不能是自己");
        }

        // 2. 不能挂到自己的子孙下面（会造成环）
        if (parentId != 0L && isDescendant(parentId, id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "上级类目不能是自己的下级");
        }

        // 3. 同级同名校验
        if (productCategoryMapper.countByName(parentId, name, id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "同级下已存在同名类目");
        }

        // 4. 计算新的层级
        int level = 1;
        if (parentId != 0L) {
            ProductCategory parent = productCategoryMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "上级类目不存在");
            }
            level = (parent.getCategoryLevel() == null ? 1 : parent.getCategoryLevel()) + 1;
        }

        // 5. 更新
        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setName(name);
        category.setCategoryLevel(level);
        category.setSerialNo(dto.getSerialNo());
        category.setSort(dto.getSort());
        category.setRemark(dto.getRemark());
        category.setUpdatedBy(currentUserId());
        category.setUpdateTime(LocalDateTime.now());
        productCategoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        ProductCategory existing = productCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "类目不存在或已删除");
        }

        // 1. 有子类目不能删
        if (productCategoryMapper.countChildren(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在子类目，无法删除");
        }

        // 2. 类目下有商品不能删
        if (productCategoryMapper.countProducts(id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "该类目下存在商品，无法删除");
        }

        // 3. 软删，deleted_token 写唯一值，保证删掉后同名可以再建
        productCategoryMapper.softDelete(id,
                UUID.randomUUID().toString().replace("-", ""),
                currentUserId(),
                LocalDateTime.now());
    }

    /**
     * 判断 candidateId 是否是 ancestorId 的子孙。
     * 做法：自底向上找 candidateId 的祖先链，看有没有撞上 ancestorId。
     */
    private boolean isDescendant(Long candidateId, Long ancestorId) {
        Map<Long, ProductCategory> allMap = new LinkedHashMap<>();
        List<ProductCategory> all = productCategoryMapper.selectAll();
        for (ProductCategory category : all) {
            allMap.put(category.getId(), category);
        }

        Long current = candidateId;
        // 最多循环层级数，防止数据异常时死循环
        for (int i = 0; i < all.size() + 1; i++) {
            if (current == null || current == 0L) {
                return false;
            }
            if (current.equals(ancestorId)) {
                return true;
            }
            ProductCategory node = allMap.get(current);
            if (node == null) {
                return false;
            }
            current = node.getParentId();
        }
        return false;
    }

    /**
     * 把平铺的类目组装成树。用 LinkedHashMap 保证顺序稳定。
     */
    private List<ProductCategoryVO> buildTree(List<ProductCategory> all) {
        Map<Long, ProductCategoryVO> nodeMap = new LinkedHashMap<>();
        for (ProductCategory category : all) {
            nodeMap.put(category.getId(), toVO(category));
        }

        List<ProductCategoryVO> roots = new ArrayList<>();
        for (ProductCategory category : all) {
            ProductCategoryVO node = nodeMap.get(category.getId());
            Long parentId = category.getParentId() == null ? 0L : category.getParentId();
            ProductCategoryVO parent = parentId == 0L ? null : nodeMap.get(parentId);
            if (parent == null) {
                // 找不到父节点（顶级，或父节点已被删除），当作顶级挂出去
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /** 实体转 VO */
    private ProductCategoryVO toVO(ProductCategory category) {
        ProductCategoryVO vo = new ProductCategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setSerialNo(category.getSerialNo());
        vo.setSort(category.getSort());
        vo.setRemark(category.getRemark());
        vo.setCategoryLevel(category.getCategoryLevel());
        return vo;
    }

    /**
     * 取当前登录人 id。JWT 鉴权未接入时先返回 null，不阻塞功能。
     */
    private Long currentUserId() {
        return null;
    }
}

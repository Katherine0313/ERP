package com.erp.service.impl;

import com.erp.common.exception.BusinessException;
import com.erp.common.result.PageResult;
import com.erp.dto.ProductBatchEditDTO;
import com.erp.dto.ProductPriceDTO;
import com.erp.dto.ProductQueryDTO;
import com.erp.dto.ProductSaveDTO;
import com.erp.dto.ProductSkuAttributeDTO;
import com.erp.dto.ProductSkuDTO;
import com.erp.dto.ProductSkuUnitDTO;
import com.erp.entity.Product;
import com.erp.entity.ProductAttributeGroup;
import com.erp.entity.ProductAttributeValue;
import com.erp.entity.ProductPrice;
import com.erp.entity.ProductSku;
import com.erp.entity.ProductUnit;
import com.erp.mapper.ProductAttributeMapper;
import com.erp.mapper.ProductAttributeRelMapper;
import com.erp.mapper.ProductMapper;
import com.erp.mapper.ProductPriceMapper;
import com.erp.mapper.ProductSkuAttributeRelMapper;
import com.erp.mapper.ProductSkuMapper;
import com.erp.mapper.ProductSkuUnitMapper;
import com.erp.mapper.ProductUnitMapper;
import com.erp.service.ProductService;
import com.erp.vo.ProductDetailVO;
import com.erp.vo.ProductImportResultVO;
import com.erp.vo.ProductListVO;
import com.erp.vo.ProductSkuVO;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 商品服务。
 *
 * <p>商品保存是聚合写操作：主表软删语义、从属表（属性组关联 / SKU / 属性值关联 /
 * 多单位 / 价格）用物理删除重建，整体放在一个事务里。
 */
@Service
public class ProductServiceImpl implements ProductService {

    /** SKU 组合数上限，与前端常量保持一致 */
    private static final int MAX_SKU_COUNT = 50;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    private ProductAttributeRelMapper productAttributeRelMapper;

    @Resource
    private ProductSkuAttributeRelMapper productSkuAttributeRelMapper;

    @Resource
    private ProductSkuUnitMapper productSkuUnitMapper;

    @Resource
    private ProductPriceMapper productPriceMapper;

    @Resource
    private ProductAttributeMapper productAttributeMapper;

    @Resource
    private ProductUnitMapper productUnitMapper;

    @Override
    public PageResult<ProductListVO> page(ProductQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }
        int offset = (pageNum - 1) * pageSize;
        long total = productMapper.countPage(query);
        List<ProductListVO> records = productMapper.selectPage(query, offset, pageSize);
        return PageResult.of(total, records);
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在或已删除");
        }

        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setProductNo(product.getProductNo());
        vo.setCategoryId(product.getCategoryId());
        vo.setName(product.getName());
        vo.setMfrs(product.getMfrs());
        vo.setBrand(product.getBrand());
        vo.setMnemonic(product.getMnemonic());
        vo.setUnitId(product.getUnitId());
        vo.setExpiryNum(product.getExpiryNum());
        vo.setWeight(product.getWeight());
        vo.setImgName(product.getImgName());
        vo.setRemark(product.getRemark());
        vo.setStatus(product.getStatus());
        vo.setEnableSerialNumber(product.getEnableSerialNumber());
        vo.setEnableBatchNumber(product.getEnableBatchNumber());
        vo.setAttributeGroupIds(productAttributeRelMapper.selectGroupIdsByProductId(id));

        List<ProductSku> skus = productSkuMapper.selectByProductId(id);
        List<ProductSkuVO> skuVOs = new ArrayList<>();
        for (ProductSku sku : skus) {
            ProductSkuVO skuVO = new ProductSkuVO();
            skuVO.setId(sku.getId());
            skuVO.setSkuCode(sku.getSkuCode());
            skuVO.setBarCode(sku.getBarCode());
            skuVO.setAttrSignature(sku.getAttrSignature());
            skuVO.setDefaultFlag(sku.getDefaultFlag());
            skuVO.setEnabled(sku.getEnabled());
            skuVO.setAttributes(productSkuAttributeRelMapper.selectBySkuId(sku.getId()));
            skuVO.setPrices(productPriceMapper.selectBySkuId(sku.getId()));
            skuVO.setUnits(productSkuUnitMapper.selectBySkuId(sku.getId()));
            skuVOs.add(skuVO);
        }
        vo.setSkus(skuVOs);
        return vo;
    }

    @Override
    public List<ProductListVO> listOptions() {
        return productMapper.selectOptions();
    }

    @Override
    public byte[] exportExcel(ProductQueryDTO query) {
        List<ProductListVO> list = productMapper.selectForExport(query);
        String[] headers = {
                "商品编码", "商品名称", "助记码", "商品类目", "默认单位", "规格数",
                "采购价", "零售价", "销售价", "最低售价", "库存数量", "成本价",
                "状态", "序列号", "批次", "创建时间",
        };

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("商品信息");

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < list.size(); i++) {
                ProductListVO vo = list.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(nullToEmpty(vo.getProductNo()));
                row.createCell(1).setCellValue(nullToEmpty(vo.getName()));
                row.createCell(2).setCellValue(nullToEmpty(vo.getMnemonic()));
                row.createCell(3).setCellValue(nullToEmpty(vo.getCategoryName()));
                row.createCell(4).setCellValue(nullToEmpty(vo.getUnitName()));
                row.createCell(5).setCellValue(vo.getSkuCount() == null ? 0 : vo.getSkuCount());
                setPriceCell(row, 6, vo.getPurchasePrice());
                setPriceCell(row, 7, vo.getRetailPrice());
                setPriceCell(row, 8, vo.getSalePrice());
                setPriceCell(row, 9, vo.getLowPrice());
                setPriceCell(row, 10, vo.getStockQuantity());
                setPriceCell(row, 11, vo.getCostPrice());
                row.createCell(12).setCellValue(statusLabel(vo.getStatus()));
                row.createCell(13).setCellValue("1".equals(vo.getEnableSerialNumber()) ? "是" : "否");
                row.createCell(14).setCellValue("1".equals(vo.getEnableBatchNumber()) ? "是" : "否");
                row.createCell(15).setCellValue(vo.getCreateTime() == null
                        ? "" : vo.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("生成商品信息 Excel 失败", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void setPriceCell(XSSFRow row, int index, BigDecimal value) {
        Cell cell = row.createCell(index);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
    }

    private String statusLabel(String status) {
        if ("0".equals(status)) {
            return "草稿";
        }
        if ("1".equals(status)) {
            return "启用";
        }
        if ("2".equals(status)) {
            return "停售";
        }
        if ("3".equals(status)) {
            return "淘汰";
        }
        return "";
    }

    @Override
    public List<ProductUnit> listUnits() {
        return productUnitMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductSaveDTO dto) {
        doSave(null, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductSaveDTO dto) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在或已删除");
        }
        doSave(id, dto);
    }

    @Override
    public void delete(Long id) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在或已删除");
        }
        // TODO 单据域建好后补「有单据引用的商品不允许删除」校验
        productMapper.softDelete(id,
                UUID.randomUUID().toString().replace("-", ""),
                currentUserId(), LocalDateTime.now());
    }

    @Override
    public void updateStatus(Long id, String status) {
        if (status == null || !Arrays.asList("0", "1", "2", "3").contains(status)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商品状态不合法");
        }
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在或已删除");
        }
        productMapper.updateStatus(id, status, currentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, String status) {
        checkIds(ids);
        if (status == null || !Arrays.asList("0", "1", "2", "3").contains(status)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商品状态不合法");
        }
        productMapper.batchUpdateStatus(ids, status, currentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchEdit(List<Long> ids, ProductBatchEditDTO dto) {
        checkIds(ids);
        if (dto == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "没有要修改的内容");
        }
        if (dto.getCategoryId() == null && isEmpty(dto.getBrand()) && isEmpty(dto.getMfrs())
                && isEmpty(dto.getMnemonic()) && isEmpty(dto.getStatus()) && isEmpty(dto.getRemark())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请至少填写一个要修改的字段");
        }
        productMapper.batchEdit(ids, dto, currentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStock(List<Long> ids, BigDecimal quantity) {
        checkIds(ids);
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存数量不能为空且不能为负数");
        }
        productMapper.batchUpdateStock(ids, quantity, currentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCost(List<Long> ids, BigDecimal cost) {
        checkIds(ids);
        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "成本价不能为空且不能为负数");
        }
        productMapper.batchUpdateCost(ids, cost, currentUserId(), LocalDateTime.now());
    }

    private void checkIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先选择要操作的商品");
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductImportResultVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择要导入的 Excel 文件");
        }
        ProductImportResultVO result = new ProductImportResultVO();
        LocalDateTime now = LocalDateTime.now();
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            result.setTotal(Math.max(lastRow, 0));
            for (int i = 1; i <= lastRow; i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    importRow(row, now);
                    result.setSuccess(result.getSuccess() + 1);
                } catch (Exception e) {
                    result.setFail(result.getFail() + 1);
                    if (result.getMessages().size() < 20) {
                        result.getMessages().add("第 " + (i + 1) + " 行：" + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取导入文件失败", e);
        }
        return result;
    }

    /**
     * 导入一行：按商品编码 upsert 主表 + 默认 SKU + 四类价格。
     * 每行独立 try-catch，坏行不影响其他行。
     */
    private void importRow(XSSFRow row, LocalDateTime now) {
        String productNo = cellText(row.getCell(0)).trim();
        String name = cellText(row.getCell(1)).trim();
        if (productNo.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("商品编码和名称不能为空");
        }
        String mnemonic = cellText(row.getCell(2)).trim();
        String categoryName = cellText(row.getCell(3)).trim();
        String unitName = cellText(row.getCell(4)).trim();
        String status = normalizeStatus(cellText(row.getCell(5)).trim());
        BigDecimal purchase = cellDecimal(row.getCell(6));
        BigDecimal retail = cellDecimal(row.getCell(7));
        BigDecimal sale = cellDecimal(row.getCell(8));
        BigDecimal low = cellDecimal(row.getCell(9));
        BigDecimal cost = cellDecimal(row.getCell(10));
        BigDecimal stock = cellDecimal(row.getCell(11));
        String brand = cellText(row.getCell(12)).trim();
        String mfrs = cellText(row.getCell(13)).trim();
        String remark = cellText(row.getCell(14)).trim();

        Long categoryId = categoryName.isEmpty() ? null : productMapper.selectCategoryIdByName(categoryName);
        Long unitId = unitName.isEmpty() ? null : productMapper.selectUnitIdByName(unitName);

        Product existing = productMapper.selectByProductNo(productNo);
        Long productId;
        Long skuId;
        if (existing == null) {
            Product product = new Product();
            product.setProductNo(productNo);
            product.setName(name);
            product.setMnemonic(mnemonic);
            product.setCategoryId(categoryId);
            product.setUnitId(unitId);
            product.setBrand(brand);
            product.setMfrs(mfrs);
            product.setRemark(remark);
            product.setStatus(status == null ? "1" : status);
            product.setStockQuantity(stock == null ? BigDecimal.ZERO : stock);
            product.setCostPrice(cost);
            product.setEnableSerialNumber("0");
            product.setEnableBatchNumber("0");
            product.setCreatedBy(null);
            product.setUpdatedBy(null);
            product.setCreateTime(now);
            product.setUpdateTime(now);
            productMapper.insert(product);
            productId = product.getId();
            skuId = createDefaultSku(productId, productNo, now);
        } else {
            existing.setName(name);
            existing.setMnemonic(mnemonic);
            existing.setCategoryId(categoryId);
            existing.setUnitId(unitId);
            existing.setBrand(brand);
            existing.setMfrs(mfrs);
            existing.setRemark(remark);
            if (status != null) {
                existing.setStatus(status);
            }
            if (stock != null) {
                existing.setStockQuantity(stock);
            }
            if (cost != null) {
                existing.setCostPrice(cost);
            }
            existing.setUpdatedBy(null);
            existing.setUpdateTime(now);
            productMapper.update(existing);
            productId = existing.getId();

            List<ProductSku> skus = productSkuMapper.selectByProductId(productId);
            if (skus.isEmpty()) {
                skuId = createDefaultSku(productId, productNo, now);
            } else {
                skuId = skus.get(0).getId();
            }
        }

        Map<String, BigDecimal> prices = new LinkedHashMap<>();
        prices.put("1", purchase);
        prices.put("2", retail);
        prices.put("3", sale);
        prices.put("4", low);
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            BigDecimal value = entry.getValue();
            if (value == null) {
                continue;
            }
            Long priceId = productPriceMapper.selectIdForImport(skuId, entry.getKey());
            if (priceId != null) {
                productPriceMapper.updatePriceValue(priceId, value, null, now);
            } else {
                ProductPrice price = new ProductPrice();
                price.setProductId(productId);
                price.setSkuId(skuId);
                price.setUnitId(0L);
                price.setPriceType(entry.getKey());
                price.setPartnerType("0");
                price.setPartnerId(0L);
                price.setCustomerLevel("");
                price.setPrice(value);
                price.setIncludeTax("0");
                price.setEnabled(true);
                price.setCreatedBy(null);
                price.setUpdatedBy(null);
                price.setCreateTime(now);
                price.setUpdateTime(now);
                productPriceMapper.insert(price);
            }
        }
    }

    private Long createDefaultSku(Long productId, String productNo, LocalDateTime now) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        sku.setSkuCode(productNo + "-1");
        sku.setAttrSignature("0");
        sku.setDefaultFlag("1");
        sku.setEnabled(true);
        sku.setCreatedBy(null);
        sku.setUpdatedBy(null);
        sku.setCreateTime(now);
        sku.setUpdateTime(now);
        productSkuMapper.insert(sku);
        return sku.getId();
    }

    @Override
    public byte[] importTemplate() {
        String[] headers = {
                "商品编码*", "商品名称*", "助记码", "类目名称", "默认单位", "状态",
                "采购价", "零售价", "销售价", "最低售价", "成本价", "库存数量", "品牌", "制造商", "备注",
        };
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("商品导入模板");
            XSSFRow header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            XSSFRow sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("P9001");
            sample.createCell(1).setCellValue("示例商品");
            sample.createCell(5).setCellValue("启用");
            sample.createCell(7).setCellValue(100);
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("生成导入模板失败", e);
        }
    }

    private String normalizeStatus(String text) {
        if (text.isEmpty()) {
            return null;
        }
        if ("启用".equals(text)) {
            return "1";
        }
        if ("草稿".equals(text)) {
            return "0";
        }
        if ("停售".equals(text) || "禁用".equals(text)) {
            return "2";
        }
        if ("淘汰".equals(text)) {
            return "3";
        }
        if (text.matches("[0-3]")) {
            return text;
        }
        return null;
    }

    private String cellText(XSSFCell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        }
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BOOLEAN) {
            return cell.getBooleanCellValue() ? "1" : "0";
        }
        return "";
    }

    private BigDecimal cellDecimal(XSSFCell cell) {
        String text = cellText(cell).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值格式错误：" + text);
        }
    }

    @Override
    public List<Product> list() {
        return productMapper.selectAllProducts();
    }

    /**
     * 聚合保存。
     *
     * @param id 为 null 表示新增，否则修改
     */
    private void doSave(Long id, ProductSaveDTO dto) {
        String productNo = dto.getProductNo() == null ? "" : dto.getProductNo().trim();
        if (productNo.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商品编码不能为空");
        }

        // 1. 编码唯一（排除自身）
        if (productMapper.countByProductNo(productNo, id) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "商品编码已存在");
        }

        // 2. 校验属性组：必须存在且启用
        Set<Long> enabledGroupIds = new HashSet<>();
        List<Long> groupIds = dto.getAttributeGroupIds() == null
                ? new ArrayList<>() : dto.getAttributeGroupIds();
        for (Long groupId : groupIds) {
            ProductAttributeGroup group = productAttributeMapper.selectGroupById(groupId);
            if (group == null || !Boolean.TRUE.equals(group.getEnabled())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "属性组不存在或已停用");
            }
            enabledGroupIds.add(groupId);
        }

        // 3. 校验 SKU：组合数、属性值归属、每组一值、组合不重复；签名由服务端重算
        List<ProductSkuDTO> skus = dto.getSkus() == null ? new ArrayList<>() : dto.getSkus();
        if (skus.size() > MAX_SKU_COUNT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "规格组合数超过 " + MAX_SKU_COUNT + "，请减少属性组或属性值");
        }
        if (skus.isEmpty()) {
            // 无属性商品也要有一条默认 SKU，库存/价格/条码统一走 SKU 维度
            ProductSkuDTO defaultSku = new ProductSkuDTO();
            defaultSku.setDefaultFlag("1");
            defaultSku.setEnabled(true);
            skus.add(defaultSku);
        }

        Set<String> signatures = new HashSet<>();
        for (ProductSkuDTO sku : skus) {
            List<ProductSkuAttributeDTO> attributes = sku.getAttributes() == null
                    ? new ArrayList<>() : sku.getAttributes();
            Set<Long> groupInSku = new HashSet<>();
            List<Long> valueIds = new ArrayList<>();
            for (ProductSkuAttributeDTO attr : attributes) {
                Long groupId = attr.getGroupId();
                if (!enabledGroupIds.contains(groupId)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "SKU 引用了未启用的属性组");
                }
                if (!groupInSku.add(groupId)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "同一 SKU 在同一属性组只能选一个值");
                }
                ProductAttributeValue value = productAttributeMapper.selectValueById(attr.getValueId());
                if (value == null || !groupId.equals(value.getGroupId())) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "属性值不属于对应的属性组");
                }
                valueIds.add(attr.getValueId());
            }
            String signature = calcAttrSignature(valueIds);
            if (!signatures.add(signature)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "存在重复的属性组合");
            }
            sku.setAttrSignature(signature);
        }

        // 4. 写主表
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserId();
        Product product = new Product();
        product.setId(id);
        product.setProductNo(productNo);
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName() == null ? "" : dto.getName().trim());
        product.setMfrs(dto.getMfrs());
        product.setBrand(dto.getBrand());
        product.setMnemonic(dto.getMnemonic());
        product.setUnitId(dto.getUnitId());
        product.setExpiryNum(dto.getExpiryNum());
        product.setWeight(dto.getWeight());
        product.setStockQuantity(dto.getStockQuantity() == null ? BigDecimal.ZERO : dto.getStockQuantity());
        product.setCostPrice(dto.getCostPrice());
        product.setImgName(dto.getImgName());
        product.setRemark(dto.getRemark());
        product.setStatus(dto.getStatus() == null ? "1" : dto.getStatus());
        product.setEnableSerialNumber(dto.getEnableSerialNumber() == null ? "0" : dto.getEnableSerialNumber());
        product.setEnableBatchNumber(dto.getEnableBatchNumber() == null ? "0" : dto.getEnableBatchNumber());
        product.setCreatedBy(userId);
        product.setUpdatedBy(userId);
        product.setCreateTime(now);
        product.setUpdateTime(now);

        if (id == null) {
            productMapper.insert(product);
            id = product.getId();
        } else {
            productMapper.update(product);
        }

        // 5. 从属数据物理删除重建
        productAttributeRelMapper.physicalDeleteByProductId(id);
        productSkuMapper.physicalDeleteByProductId(id);
        productSkuAttributeRelMapper.physicalDeleteByProductId(id);
        productSkuUnitMapper.physicalDeleteByProductId(id);
        productPriceMapper.physicalDeleteByProductId(id);

        int sort = 1;
        for (Long groupId : groupIds) {
            productAttributeRelMapper.insert(id, groupId, String.valueOf(sort++), userId, now);
        }

        for (ProductSkuDTO skuDto : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(id);
            sku.setSkuCode(skuDto.getSkuCode());
            sku.setBarCode(skuDto.getBarCode());
            sku.setAttrSignature(skuDto.getAttrSignature());
            sku.setDefaultFlag(skuDto.getDefaultFlag() == null ? "0" : skuDto.getDefaultFlag());
            sku.setEnabled(skuDto.getEnabled() == null ? Boolean.TRUE : skuDto.getEnabled());
            sku.setCreatedBy(userId);
            sku.setUpdatedBy(userId);
            sku.setCreateTime(now);
            sku.setUpdateTime(now);
            productSkuMapper.insert(sku);
            Long skuId = sku.getId();

            List<ProductSkuAttributeDTO> attributes = skuDto.getAttributes() == null
                    ? new ArrayList<>() : skuDto.getAttributes();
            for (ProductSkuAttributeDTO attr : attributes) {
                productSkuAttributeRelMapper.insert(skuId, id, attr.getGroupId(), attr.getValueId(), userId, now);
            }

            List<ProductSkuUnitDTO> units = skuDto.getUnits() == null
                    ? new ArrayList<>() : skuDto.getUnits();
            for (ProductSkuUnitDTO unit : units) {
                productSkuUnitMapper.insert(skuId, id, unit.getUnitId(),
                        unit.getDefaultFlag() == null ? "0" : unit.getDefaultFlag(), userId, now);
            }

            List<ProductPriceDTO> prices = skuDto.getPrices() == null
                    ? new ArrayList<>() : skuDto.getPrices();
            for (ProductPriceDTO priceDto : prices) {
                ProductPrice price = new ProductPrice();
                price.setProductId(id);
                price.setSkuId(skuId);
                price.setUnitId(priceDto.getUnitId() == null ? 0L : priceDto.getUnitId());
                price.setPriceType(priceDto.getPriceType());
                price.setPartnerType(priceDto.getPartnerType() == null ? "0" : priceDto.getPartnerType());
                price.setPartnerId(priceDto.getPartnerId() == null ? 0L : priceDto.getPartnerId());
                price.setCustomerLevel(priceDto.getCustomerLevel() == null ? "" : priceDto.getCustomerLevel());
                price.setPrice(priceDto.getPrice() == null ? java.math.BigDecimal.ZERO : priceDto.getPrice());
                price.setTaxRate(priceDto.getTaxRate());
                price.setIncludeTax(priceDto.getIncludeTax() == null ? "0" : priceDto.getIncludeTax());
                price.setEnabled(priceDto.getEnabled() == null ? Boolean.TRUE : priceDto.getEnabled());
                price.setCreatedBy(userId);
                price.setUpdatedBy(userId);
                price.setCreateTime(now);
                price.setUpdateTime(now);
                productPriceMapper.insert(price);
            }
        }
    }

    /**
     * 计算属性组合签名：属性值 id 升序拼接后取 MD5；无属性固定为 "0"。
     */
    private String calcAttrSignature(List<Long> valueIds) {
        if (valueIds == null || valueIds.isEmpty()) {
            return "0";
        }
        List<Long> sorted = new ArrayList<>(valueIds);
        Collections.sort(sorted);
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                joined.append(',');
            }
            joined.append(sorted.get(i));
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(joined.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String part = Integer.toHexString(b & 0xff);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    /** 取当前登录人 id。JWT 上下文未接入时先返回 null，不阻塞功能。 */
    private Long currentUserId() {
        return null;
    }
}

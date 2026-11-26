import request from '../request.js'

/**
 * 分页查询商品信息
 */
export function pageProduct(params) {
    return request.get('/api/admin/product/page', { params })
}

/**
 * 查询商品详情（属性组 / SKU / 价格 / 多单位）
 */
export function getProduct(id) {
    return request.get(`/api/admin/product/${id}`)
}

/**
 * 商品平铺下拉
 */
export function listProduct() {
    return request.get('/api/admin/product/list')
}

/**
 * 计量单位下拉
 */
export function listUnit() {
    return request.get('/api/admin/product/unit/list')
}

/**
 * 新增商品（整体保存）
 */
export function addProduct(data) {
    return request.post('/api/admin/product', data)
}

/**
 * 修改商品（从属数据整体覆盖）
 */
export function updateProduct(id, data) {
    return request.put(`/api/admin/product/${id}`, data)
}

/**
 * 删除商品（软删除）
 */
export function deleteProduct(id) {
    return request.delete(`/api/admin/product/${id}`)
}

/**
 * 导出商品信息 Excel（按当前查询条件，导出全部匹配数据）
 */
export function exportProduct(params) {
    return request.get('/api/admin/product/export', { params, responseType: 'blob' })
}

/** 把选中的 id 拼成 ids=1&ids=2 形式的查询串 */
function idsQuery(ids, extra = {}) {
    const parts = ids.map(id => `ids=${encodeURIComponent(id)}`)
    for (const key of Object.keys(extra)) {
        const value = extra[key]
        if (value !== undefined && value !== null) {
            parts.push(`${key}=${encodeURIComponent(value)}`)
        }
    }
    return parts.join('&')
}

/**
 * 批量启用/禁用
 */
export function batchProductStatus(ids, status) {
    return request.put('/api/admin/product/batch-status', { ids, status })
}

/**
 * 批量编辑（只提交填了值的字段）
 */
export function batchEditProduct(ids, data) {
    return request.put(`/api/admin/product/batch-edit?${idsQuery(ids)}`, data)
}

/**
 * 批量修正库存
 */
export function batchUpdateStock(ids, quantity) {
    return request.put(`/api/admin/product/batch-stock?${idsQuery(ids, { quantity })}`)
}

/**
 * 批量修正成本
 */
export function batchUpdateCost(ids, cost) {
    return request.put(`/api/admin/product/batch-cost?${idsQuery(ids, { cost })}`)
}

/**
 * 导入商品 Excel
 */
export function importProduct(formData) {
    return request.post('/api/admin/product/import', formData)
}

/**
 * 下载商品导入模板
 */
export function downloadImportTemplate() {
    return request.get('/api/admin/product/import/template', { responseType: 'blob' })
}

/**
 * 商品上下架
 */
export function updateProductStatus(id, status) {
    return request.put(`/api/admin/product/${id}/status`, null, { params: { status } })
}

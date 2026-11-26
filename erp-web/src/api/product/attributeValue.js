import request from '../request.js'

/**
 * 分页查询商品属性值
 */
export function pageAttributeValue(params) {
    return request.get('/api/admin/product/attribute-value/page', { params })
}

/**
 * 新增商品属性值
 */
export function addAttributeValue(data) {
    return request.post('/api/admin/product/attribute-value', data)
}

/**
 * 修改商品属性值
 */
export function updateAttributeValue(id, data) {
    return request.put(`/api/admin/product/attribute-value/${id}`, data)
}

/**
 * 删除商品属性值
 */
export function deleteAttributeValue(id) {
    return request.delete(`/api/admin/product/attribute-value/${id}`)
}

/**
 * 启用或停用商品属性值
 */
export function updateAttributeValueStatus(id, enabled) {
    return request.put(`/api/admin/product/attribute-value/${id}/status`, null, { params: { enabled } })
}

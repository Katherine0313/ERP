import request from '../request.js'

/**
 * 分页查询商品属性组
 */
export function pageAttributeGroup(params) {
    return request.get('/api/admin/product/attribute-group/page', { params })
}

/**
 * 查询商品属性组下拉列表
 */
export function listAttributeGroup() {
    return request.get('/api/admin/product/attribute-group/list')
}

/**
 * 新增商品属性组
 */
export function addAttributeGroup(data) {
    return request.post('/api/admin/product/attribute-group', data)
}

/**
 * 修改商品属性组
 */
export function updateAttributeGroup(id, data) {
    return request.put(`/api/admin/product/attribute-group/${id}`, data)
}

/**
 * 删除商品属性组
 */
export function deleteAttributeGroup(id) {
    return request.delete(`/api/admin/product/attribute-group/${id}`)
}

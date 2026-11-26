import request from '../request.js'

/**
 * 分页查询计量单位
 */
export function pageUnit(params) {
    return request.get('/api/admin/product/unit/page', { params })
}

/**
 * 查询基础单位下拉
 */
export function listBasicUnit() {
    return request.get('/api/admin/product/unit/basic')
}

/**
 * 新增计量单位
 */
export function addUnit(data) {
    return request.post('/api/admin/product/unit', data)
}

/**
 * 修改计量单位
 */
export function updateUnit(id, data) {
    return request.put(`/api/admin/product/unit/${id}`, data)
}

/**
 * 删除计量单位
 */
export function deleteUnit(id) {
    return request.delete(`/api/admin/product/unit/${id}`)
}

/**
 * 启用/停用计量单位
 */
export function updateUnitStatus(id, enabled) {
    return request.put(`/api/admin/product/unit/${id}/status`, null, { params: { enabled } })
}

/**
 * 批量启用/停用计量单位
 */
export function batchUnitStatus(ids, enabled) {
    return request.put('/api/admin/product/unit/batch-status', { ids, enabled })
}

import request from '../request.js'

/**
 * 查询商品类目树
 */
export function treeCategory() {
    return request.get('/api/admin/product/category/tree')
}

/**
 * 查询商品类目平铺列表（下拉用）
 */
export function listCategory() {
    return request.get('/api/admin/product/category/list')
}

/**
 * 查询商品类目详情
 */
export function getCategory(id) {
    return request.get(`/api/admin/product/category/${id}`)
}

/**
 * 新增商品类目
 */
export function addCategory(data) {
    return request.post('/api/admin/product/category', data)
}

/**
 * 修改商品类目
 */
export function updateCategory(id, data) {
    return request.put(`/api/admin/product/category/${id}`, data)
}

/**
 * 删除商品类目
 */
export function deleteCategory(id) {
    return request.delete(`/api/admin/product/category/${id}`)
}

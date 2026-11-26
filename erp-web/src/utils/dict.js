/**
 * 商品模块枚举字典。
 *
 * 与后端 product 表、product_price 表的取值严格对应。
 */

/** 商品状态：0草稿 1启用 2停售 3淘汰 */
export const PRODUCT_STATUS = [
    { value: '0', label: '草稿', color: 'default' },
    { value: '1', label: '启用', color: 'success' },
    { value: '2', label: '停售', color: 'warning' },
    { value: '3', label: '淘汰', color: 'danger' },
]

/** 价格类型：1采购 2零售 3批发 4最低 5客户等级 6客户协议 7供应商 */
export const PRICE_TYPE = [
    { value: '1', label: '采购价' },
    { value: '2', label: '零售价' },
    { value: '3', label: '销售价' },
    { value: '4', label: '最低售价' },
    { value: '5', label: '客户等级价' },
    { value: '6', label: '客户协议价' },
    { value: '7', label: '供应商价' },
]

/** 价格对象类型：0无 1客户 2供应商 */
export const PARTNER_TYPE = [
    { value: '0', label: '不限' },
    { value: '1', label: '客户' },
    { value: '2', label: '供应商' },
]

/** 是否启用：0否 1是 */
export const ENABLE_FLAG = [
    { value: '1', label: '启用', color: 'success' },
    { value: '0', label: '停用', color: 'default' },
]

/** 用户/是否标记的显示值：1是 0否（与后端 varchar 字段对应） */
export const YES_NO = [
    { value: '1', label: '是' },
    { value: '0', label: '否' },
]

/** SKU 笛卡尔积组合数上限，与后端 ProductServiceImpl.MAX_SKU_COUNT 保持一致 */
export const MAX_SKU_COUNT = 50

/**
 * 按字典取值对应的标签。
 */
export function labelOf(dict, value) {
    for (const item of dict) {
        if (item.value === value) {
            return item.label
        }
    }
    return '-'
}

/**
 * 按字典取值对应的颜色。
 */
export function colorOf(dict, value) {
    for (const item of dict) {
        if (item.value === value) {
            return item.color || 'default'
        }
    }
    return 'default'
}

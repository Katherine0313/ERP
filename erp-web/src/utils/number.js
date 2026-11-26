/**
 * 数值格式化工具。
 *
 * 精度口径与后端一致（见 doc/product 设计原则）：
 *   数量 / 换算比例  decimal(24,4)
 *   金额 / 单价      decimal(24,6)
 *   税率             decimal(10,4)
 */

/** 数量输入框精度 */
export const QUANTITY_PRECISION = 4

/** 金额输入框精度 */
export const AMOUNT_PRECISION = 6

/** 税率输入框精度 */
export const TAX_RATE_PRECISION = 4

/**
 * 金额格式化：展示保留 2 位小数。
 */
export function formatAmount(value) {
    if (value === null || value === undefined || value === '') {
        return '-'
    }
    const num = Number(value)
    if (Number.isNaN(num)) {
        return '-'
    }
    return num.toFixed(2)
}

/**
 * 数量格式化：最多保留 4 位小数，整数不带小数点。
 */
export function formatQuantity(value) {
    if (value === null || value === undefined || value === '') {
        return '-'
    }
    const num = Number(value)
    if (Number.isNaN(num)) {
        return '-'
    }
    if (Number.isInteger(num)) {
        return String(num)
    }
    return String(Number(num.toFixed(QUANTITY_PRECISION)))
}

/**
 * 比例格式化：用于单位换算比例。
 */
export function formatRatio(value) {
    if (value === null || value === undefined || value === '') {
        return '-'
    }
    const num = Number(value)
    if (Number.isNaN(num)) {
        return '-'
    }
    return String(Number(num.toFixed(2)))
}

/**
 * 税率格式化：后端存的是百分数（如 13.0000），展示成 13%。
 */
export function formatTaxRate(value) {
    if (value === null || value === undefined || value === '') {
        return '-'
    }
    const num = Number(value)
    if (Number.isNaN(num)) {
        return '-'
    }
    return `${Number(num.toFixed(2))}%`
}

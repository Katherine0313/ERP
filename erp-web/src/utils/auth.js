// ==================== 登录态解析 ====================
// token 由后端 JWT 生成，payload 内含 { id, username, roles, permissions, adminAvatar }
// 前端只存了 token，这里直接从 token 里解析出当前用户信息

import { getToken } from '@/utils/token.js'

/** 从 JWT token 中解析出当前登录用户信息，解析失败返回 null */
export function getCurrentUser() {
    try {
        const token = getToken()
        if (!token) return null
        const payload = token.split('.')[1]
        // base64url → base64（JWT 用 -_ 代替 +/）
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
        // 正确处理含中文的 payload（避免 atob 直接转成乱码）
        const json = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        )
        return JSON.parse(json)
    } catch (e) {
        return null
    }
}

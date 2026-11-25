import router from '../router/index.js'
import { message } from 'ant-design-vue'
import { getToken, removeToken } from '@/utils/token.js'

const LOGIN_PATH = '/login'

/** 解析 JWT 的 exp（秒）；解析失败视为已过期 */
function isTokenExpired(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
        // base64url 可能缺 padding，补齐后再 atob
        const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
        const payload = JSON.parse(atob(padded))
        return payload.exp ? payload.exp * 1000 <= Date.now() : false
    } catch (e) {
        return true
    }
}

/** 清除登录态，并把原因带给登录页展示 */
function clearLogin(msg) {
    removeToken()
    sessionStorage.setItem('loginMessage', msg)
    message.warning(msg)
}

/** 返回第一个可访问的后台页面路径；没有可用页面时返回 null */
function firstAccessiblePath() {
    const layout = router.options.routes.find(item => item.name === 'Layout')
    const first = layout?.children?.find(item => item.meta?.showInMenu)
    return first ? `/layout/${first.path}` : null
}

/** 需要登录的路由：显式 meta.isLogin，或位于 Layout 之下 */
function needLogin(to) {
    return to.meta.isLogin === true || to.matched.some(record => record.name === 'Layout')
}

router.beforeEach((to, from, next) => {
    let token = getToken()
    if (token && isTokenExpired(token)) {
        clearLogin('登录已过期，请重新登录')
        token = null
    }
    // 1. 已登录访问登录页：去第一个有权限的页面；没有可访问页面则退出登录
    if (to.name === 'Login' && token) {
        const fallback = firstAccessiblePath()
        if (fallback) {
            next(fallback)
        } else {
            clearLogin('当前账号没有可访问的页面，请联系管理员')
            next()
        }
    }
    // 2. 页面需要登录但没有 token：回登录页
    else if (needLogin(to) && !token) {
        next(LOGIN_PATH)
    }
    // 其他所有正常情况直接放行
    else {
        next()
    }
})

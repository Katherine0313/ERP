import request from './request.js'

export function login(data) {
    return request.post('/api/admin/login', data)
}

/**
 * 全局配置
 * 后端服务地址集中在这里，切换环境只改本文件
 */

// 后端服务地址
// 注意：真机调试时 127.0.0.1 指向手机自己，必须换成电脑的局域网 IP（如 http://192.168.1.10:8082）
//      模拟器 / H5 本地调试可用 127.0.0.1
export const BASE_URL = 'http://127.0.0.1:8082';

// 请求超时（毫秒）
export const TIMEOUT = 15000;

// token 在本地存储中的 key
export const TOKEN_KEY = 'erp_token';

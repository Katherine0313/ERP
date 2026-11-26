import { BASE_URL, TIMEOUT, TOKEN_KEY } from './config.js';

/**
 * 统一请求封装
 * App 端走原生 HTTP，不受浏览器同源策略限制，因此不需要后端开放 CORS
 *
 * @param {string} url     接口路径，如 '/api/ping'
 * @param {object} data    请求参数
 * @param {string} method  请求方法，GET / POST / PUT / DELETE
 * @param {boolean} auth   是否携带 token，默认 true
 * @returns {Promise} 成功 resolve 后端返回体，失败 reject
 */
export function request(url, data = {}, method = 'GET', auth = true) {
	return new Promise((resolve, reject) => {
		const header = {
			'Content-Type': 'application/json'
		};

		// 携带登录态
		if (auth) {
			const token = uni.getStorageSync(TOKEN_KEY);
			if (token) {
				header['Authorization'] = `Bearer ${token}`;
			}
		}

		// GET 请求参数拼到 query 上（App 端 GET 的 data 不会自动进 query）
		let requestUrl = BASE_URL + url;
		if (method === 'GET' && Object.keys(data).length > 0) {
			const query = Object.keys(data)
				.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
				.join('&');
			requestUrl += (requestUrl.includes('?') ? '&' : '?') + query;
			data = {};
		}

		uni.request({
			url: requestUrl,
			method,
			data,
			header,
			timeout: TIMEOUT,
			success: (res) => {
				// 登录态失效：清 token 并跳转登录页
				if (res.statusCode === 401) {
					uni.removeStorageSync(TOKEN_KEY);
					uni.showToast({
						title: '登录已过期，请重新登录',
						icon: 'none'
					});
					return reject(new Error('unauthorized'));
				}

				if (res.statusCode >= 200 && res.statusCode < 300) {
					return resolve(res.data);
				}

				uni.showToast({
					title: res.data?.message || `请求失败(${res.statusCode})`,
					icon: 'none'
				});
				reject(res.data);
			},
			fail: (err) => {
				uni.showToast({
					title: '网络连接失败，请检查后端服务与 BASE_URL',
					icon: 'none'
				});
				reject(err);
			}
		});
	});
}

// 常用方法的语法糖
export const get = (url, data, auth) => request(url, data, 'GET', auth);
export const post = (url, data, auth) => request(url, data, 'POST', auth);
export const put = (url, data, auth) => request(url, data, 'PUT', auth);
export const del = (url, data, auth) => request(url, data, 'DELETE', auth);

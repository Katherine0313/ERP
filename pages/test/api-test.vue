<template>
	<view class="wrap">
		<view class="card">
			<text class="label">后端地址（common/config.js）</text>
			<text class="value">{{ baseUrl }}</text>
		</view>

		<view class="card">
			<text class="label">请求路径</text>
			<input class="input" v-model="path" placeholder="/api/ping" />
			<text class="tip">GET 请求，返回后端原始响应体</text>
		</view>

		<button class="btn" type="primary" :loading="loading" @click="send">发送请求</button>

		<view class="card" v-if="result">
			<text class="label">状态码</text>
			<text class="value" :class="ok ? 'ok' : 'err'">{{ result.statusCode }} {{ ok ? 'OK' : 'FAIL' }}</text>

			<text class="label">耗时</text>
			<text class="value">{{ result.cost }} ms</text>

			<text class="label">响应体</text>
			<text class="body">{{ result.body }}</text>
		</view>
	</view>
</template>

<script>
	import { BASE_URL } from '@/common/config.js';
	import { get } from '@/common/request.js';

	export default {
		data() {
			return {
				baseUrl: BASE_URL,
				path: '/api/ping',
				loading: false,
				ok: false,
				result: null
			};
		},
		methods: {
			/**
			 * 发送一次 GET 请求，验证 App 到后端的连通性
			 */
			async send() {
				this.loading = true;
				this.result = null;
				const start = Date.now();
				try {
					const data = await get(this.path, {}, false);
					this.ok = true;
					this.result = {
						statusCode: 200,
						cost: Date.now() - start,
						body: JSON.stringify(data, null, 2)
					};
				} catch (err) {
					this.ok = false;
					this.result = {
						statusCode: (err && err.statusCode) || '—',
						cost: Date.now() - start,
						body: JSON.stringify(err, null, 2)
					};
				} finally {
					this.loading = false;
				}
			}
		}
	}
</script>

<style>
	page {
		background-color: #f5f6f8;
	}

	.wrap {
		padding: 30rpx;
	}

	.card {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 24rpx;
		margin-bottom: 24rpx;
	}

	.label {
		font-size: 24rpx;
		color: #999;
		display: block;
		margin-bottom: 10rpx;
	}

	.value {
		font-size: 30rpx;
		color: #333;
		display: block;
		word-break: break-all;
	}

	.ok {
		color: #07c160;
	}

	.err {
		color: #ff5257;
	}

	.tip {
		font-size: 22rpx;
		color: #bbb;
		display: block;
		margin-top: 10rpx;
	}

	.input {
		font-size: 30rpx;
		padding: 12rpx 0;
		border-bottom: 1px solid #eee;
	}

	.body {
		font-size: 26rpx;
		color: #333;
		display: block;
		white-space: pre-wrap;
		word-break: break-all;
	}

	.btn {
		margin-bottom: 30rpx;
	}
</style>

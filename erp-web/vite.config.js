import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

/**
 * 屏蔽启动时的多余提示：
 * 1. vite-plugin-vue-devtools 追加的 "Vue DevTools: Open http://..." 和
 *    "Press Alt+Shift+D to toggle the Vue DevTools"
 * 2. Vite 自带的 "press h + enter to show help"
 * DevTools 功能本身保留，http://localhost:5175/__devtools__/ 仍可正常访问。
 */
const silenceStartupLogs = {
  name: 'silence-startup-logs',
  enforce: 'pre',
  configureServer(server) {
    // 此刻 vue-devtools 还没覆盖 printUrls，先把原始实现存下来
    const originalPrintUrls = server.printUrls
    const originalBindCLIShortcuts = server.bindCLIShortcuts.bind(server)

    // 返回的函数会在所有 configureServer 执行完之后才调用，
    // 此时 vue-devtools 已经改写过 printUrls，才能安全还原
    return () => {
      server.printUrls = originalPrintUrls
      server.bindCLIShortcuts = (opts = {}) => originalBindCLIShortcuts({ ...opts, print: false })
    }
  },
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    silenceStartupLogs,
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
    server: {
  port: 5175,
      proxy: {
    // 本地开发统一走网关 8082，避免跨域；后端没起时把 target 换成
    '/api': { target: 'http://127.0.0.1:8082', changeOrigin: true },
    // 商品图片（后端存 /image/upload/xxx.webp 相对路径）
    // public/image/login/ 是前端自己的登录页插图，豁免代理，否则会被转发到 8082 而 500
    '/image': {
      target: 'http://127.0.0.1:8082',
      changeOrigin: true,
      bypass(req) {
        if (req.url && req.url.startsWith('/image/login/')) {
          return req.url
        }
      },
    },
    // 用户头像等上传文件
    '/upload': { target: 'http://127.0.0.1:8082', changeOrigin: true },
  },
},
})

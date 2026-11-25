import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.css' // 全量样式（含 reset）
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import App from './App.vue'
import router from './router'
import './router/permission.js'

// 日期类组件底层用 dayjs，不设置的话星期/月份会显示英文
dayjs.locale('zh-cn')

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd) // 全量注册所有 ant-design-vue 组件

app.mount('#app')

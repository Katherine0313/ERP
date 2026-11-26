<script setup>
import { ref, onMounted } from 'vue'
import { login } from '@/api/login.js'
import { setToken } from '@/utils/token.js'
import { message } from 'ant-design-vue'
import router from '@/router/index.js'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
const loginForm = ref({
  username: 'admin',
  password: 'admin123',
})
const loading = ref(false)

const mainImage = '/image/admin_login/login-main.webp'
const avatarImage = '/image/admin_login/jiemi.webp'

onMounted(() => {
  const msg = sessionStorage.getItem('loginMessage')
  if (msg) {
    sessionStorage.removeItem('loginMessage')
    message.warning(msg)
  }
})

const handleLogin = () => {
  loading.value = true
  login(loginForm.value)
    .then((res) => {
      setToken(res.data.data.token)
      message.success('登录成功')
      router.push({ path: '/layout/home' })
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}
</script>

<template>
  <div class="login-container">
  <div class="login-warp">
    <div class="login-intro">
      <h1 class="intro-title">为中小企业而生的轻量级ERP管理软件</h1>
      <p class="intro-sub">1秒开单打印，手机APP随时查库存、看利润</p>
      <div class="login-main">
        <img :src="mainImage" alt="login-main" />
      </div>
      <p class="intro-caption">让传统ERP向小微转型，与客户相通</p>
    </div>
    <div class="login-box">
      <div class="login-head">
        <div class="login-avatar">
          <img :src="avatarImage" alt="jiemi" />
        </div>
        <div class="login-title">杰米ERP管理系统</div>
      </div>

      <div class="form-wrapper">
        <a-form
            :model="loginForm"
            class="login-form"
            :label-col="{ style: { width: '90px' } }"
            :wrapper-col="{ style: { width: '280px' } }"
        >
          <a-form-item >
            <a-input v-model:value="loginForm.username" :disabled="loading" size="large">
              <!-- 账号前置图标 -->
              <template #prefix>
                <UserOutlined style="margin-right: 10px;" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item >
            <a-input-password v-model:value="loginForm.password" :disabled="loading" size="large">
              <!-- 密码前置图标 -->
              <template #prefix>
                <LockOutlined style="margin-right: 10px"/>
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="remember" label=" " :colon="false" :style="{ marginBottom: '12px' }">
            <a-checkbox v-model:checked="loginForm.remember">记住密码</a-checkbox>
          </a-form-item>
          <a-form-item label=" " :colon="false">
            <a-button type="primary" block :loading="loading" @click="handleLogin">登录</a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
  </div>
</template>


<style scoped>
/* 页面背景：浅蓝到近白的纵向渐变，底部渐隐 */
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #c9d8f2 0%, #e3ecfa 42%, #f7fafd 100%);
  overflow: hidden;
}
.login-warp{
  width: 100%;
  max-width: 1100px;
  padding: 0 24px;
  box-sizing: border-box;
  gap:100px;
  min-height:100vh;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  margin: auto;
}
/* 左侧介绍区：标题 + 副标题 + 主图 + 说明 */
.login-intro{
  flex: 1 1 420px;
  min-width: 0;
  text-align: center;
}
.intro-title{
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #1a4fd6;
  line-height: 1.4;
}
.intro-sub{
  margin: 0 0 24px;
  font-size: 15px;
  color: #4a6fa5;
}
/* 整个表单块的宽度 */
.login-box{
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 42px 36px;
  border-radius: 8px;
  box-shadow: 0 6px 24px rgba(26, 79, 214, 0.10);
}

/* antd 的字号加在内层 <label> 上（.ant-form-item-label > label），
   只改外层容器不会生效，必须带上子选择器 */
:deep(.ant-form-item-label > label){
  font-size: 20px;
}

.login-avatar{
  width:60px;
  height:60px;
  border-radius: 50%;
  overflow: hidden;
}
.login-avatar img{
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.login-main{
  width: 100%;
  height:300px;
  object-fit: cover;
  overflow: hidden;
}
.login-main img{
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.intro-caption{
  margin: 14px 0 0;
  font-size: 14px;
  color: #6b87b5;
}
.login-head{
  display: flex;
  gap: 50px;
  padding: 0 0 36px 0;
  align-items: center;
}
.login-title{
  margin-bottom: 20px;
  font-size:24px;
}
/* 标签列宽是由 a-form 的 :label-col 以行内样式写死的（width: 90px），
   行内样式优先级高于样式表规则，不加 !important 覆盖不掉 */
:deep(.ant-form-horizontal .ant-form-item-label){
  width: 0 !important;
}
:deep(.ant-btn){
  height: 40px;
  border-radius: 5px;
}
/* 中等屏：两列间距收窄
   断点取 861px：两列并排所需最小宽度 = 主图基准 320 + 间距 40 + 卡片 400 + 左右内边距 48 = 808px，
   断点必须高于这个折行阈值，否则会出现「已折行但仍按双列样式渲染」的夹缝区间 */
@media (min-width: 861px) and (max-width: 1024px) {
  .login-warp{
    gap: 40px;
  }
}
/* 窄屏（平板竖屏 / 手机）：改为上下单列，主图在上、表单在下 */
@media (max-width: 860px) {
  .login-warp{
    flex-direction: column;
    justify-content: center;
    gap: 24px;
    padding: 24px 16px;
    height: auto;
  }
  .login-intro{
    flex: none;
    width: 100%;
  }
  .intro-title{
    font-size: 20px;
  }
  .intro-sub{
    font-size: 13px;
    margin-bottom: 16px;
  }
  .login-main{
    width: 100%;
    height: 180px;
  }
  .login-head{
    gap: 16px;
    padding: 0 0 24px 0;
  }
  /* 卡片内边距在窄屏上收窄，否则 320px 视口下输入框只剩 201px 宽 */
  .login-box{
    padding: 28px 20px;
  }
}
</style>

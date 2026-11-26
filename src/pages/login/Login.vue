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
    <div class="login-main">
      <img :src="mainImage" alt="login-main" />
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
.login-container {
  min-height: 100vh;
  background:  #728DE8;
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
/* 整个表单块的宽度 */
.login-box{
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 42px 36px;
  border-radius: 8px;
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
  flex: 1 1 320px;
  min-width: 0;
  height:300px;
  object-fit: cover;
  overflow: hidden;
}
.login-main img{
  width: 100%;
  height: 100%;
  object-fit: cover;
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
/* 中等屏：两列间距收窄，避免 grid 轨道被内容撑破后横向溢出 */
@media (min-width: 768px) and (max-width: 1024px) {
  .login-warp{
    gap: 40px;
  }
}
/* 窄屏（平板竖屏 / 手机）：改为上下单列，主图在上、表单在下 */
@media (max-width: 767px) {
  .login-warp{
    flex-direction: column;
    justify-content: center;
    gap: 24px;
    padding: 24px 16px;
    height: auto;
  }
  .login-main{
    flex: none;
    width: 100%;
    height: 180px;
  }
  .login-head{
    padding: 0 0 24px 0;
  }
}
</style>

<script setup>
import {ref} from "vue";
import {login} from "@/api/login.js";

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
});
const handleLogin = () => {
  login(loginForm.value)
}
</script>

<template>
  <div class="login-warp">
    <div class="login-box">
      <div class="login-title">登录</div>
      <!-- label-col 控标签列宽，wrapper-col 控输入框列宽，两者之和即表单内容宽度 -->
      <div class="form-wrapper">
      <a-form
        :model="loginForm"
        class="login-form"
        :label-col="{ style: { width: '90px' } }"
        :wrapper-col="{ style: { width: '280px' } }"
      >
        <a-form-item label="账号" >
          <a-input v-model:value="loginForm.username"></a-input>
        </a-form-item>
        <a-form-item label="密码" >
          <a-input v-model:value="loginForm.password"></a-input>
        </a-form-item>
        <!-- label=" " 是空标签占位：让这两项也渲染出 90px 的标签列，
             否则没有 label 的 form-item 会顶到最左边，和上面的输入框错开；
             :colon="false" 去掉空标签后面那个多余的冒号 -->
        <a-form-item name="remember" label=" " :colon="false" :style="{ marginBottom: '12px' }">
          <a-checkbox>记住密码</a-checkbox>
        </a-form-item>
        <a-form-item label=" " :colon="false">
          <a-button type="primary" block @click="handleLogin">登录</a-button>
        </a-form-item>
      </a-form>
      </div>
    </div>
  </div>

</template>

<style scoped>
.login-warp{
  height:100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 整个表单块的宽度 */
.login-box{
  width: 400px;
}

/* antd 的字号加在内层 <label> 上（.ant-form-item-label > label），
   只改外层容器不会生效，必须带上子选择器 */
:deep(.ant-form-item-label > label){
  font-size: 20px;
}
.login-title{
  margin-left: 200px;
  margin-bottom: 20px;
  font-size:24px;
}

</style>

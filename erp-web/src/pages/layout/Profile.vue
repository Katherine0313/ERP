<script setup>
import { reactive, ref } from 'vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { getCurrentUser } from '@/utils/auth.js'

const currentUser = getCurrentUser() || {}
const activeTab = ref('basic')

const basicForm = reactive({
  username: currentUser.username || '',
  name: currentUser.name || '',
  phone: currentUser.phone || '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loginLogs = []
const loginLogColumns = [
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 190 },
  { title: '类型', dataIndex: 'loginType', key: 'loginType', width: 100 },
  { title: '结果', dataIndex: 'resultCode', key: 'resultCode', width: 90 },
  { title: 'IP 地址', dataIndex: 'ip', width: 150 },
  { title: '设备', dataIndex: 'userAgent', key: 'userAgent' },
]
</script>

<template>
  <div class="profile-page">
    <div class="profile-card">
      <a-avatar :size="72" :src="currentUser.adminAvatar || undefined">
        <template #icon><UserOutlined /></template>
      </a-avatar>
      <div class="profile-card-info">
        <div class="profile-card-name">
          {{ currentUser.name || currentUser.username || '未登录' }}
        </div>
        <div v-if="currentUser.roles?.length" class="profile-card-meta">
          <a-tag v-for="role in currentUser.roles" :key="role" color="blue">{{ role }}</a-tag>
        </div>
        <div class="profile-card-sub">
          <span>@{{ currentUser.username || '-' }}</span>
          <span>{{ basicForm.phone || '手机号未绑定' }}</span>
        </div>
      </div>
    </div>

    <a-tabs v-model:activeKey="activeTab" class="profile-tabs" size="large">
      <a-tab-pane key="basic" tab="基本资料">
        <a-form
            :model="basicForm"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 14 }"
            class="profile-form"
        >
          <a-form-item label="用户名">
            <a-input v-model:value="basicForm.username" disabled />
          </a-form-item>
          <a-form-item label="姓名">
            <a-input v-model:value="basicForm.name" placeholder="请输入姓名" :maxlength="32" />
          </a-form-item>
          <a-form-item label="手机号">
            <a-input v-model:value="basicForm.phone" placeholder="请输入手机号" :maxlength="11" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4, span: 14 }">
            <a-button type="primary" disabled>保存</a-button>
          </a-form-item>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="password" tab="修改密码">
        <a-form
            :model="passwordForm"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 14 }"
            class="profile-form"
        >
          <a-form-item label="旧密码">
            <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码" />
          </a-form-item>
          <a-form-item label="新密码">
            <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认新密码">
            <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4, span: 14 }">
            <a-button type="primary" danger disabled>确认修改</a-button>
          </a-form-item>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="logins" tab="登录日志">
        <a-table
            :data-source="loginLogs"
            :columns="loginLogColumns"
            :pagination="false"
            row-key="id"
            size="middle"
            bordered
        >
          <template #emptyText>
            <div class="empty-logs">暂无登录记录</div>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 880px;
  padding: 24px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  margin-bottom: 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.profile-card-info {
  flex: 1;
}

.profile-card-name {
  margin-bottom: 6px;
  font-size: 20px;
  font-weight: 600;
}

.profile-card-meta {
  margin-bottom: 6px;
}

.profile-card-sub {
  display: flex;
  gap: 18px;
  color: #888;
  font-size: 13px;
}

.profile-tabs {
  padding: 0 24px 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.profile-tabs :deep(.ant-tabs-nav) {
  padding: 0 24px;
  margin: 0 -24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.profile-form {
  padding-top: 12px;
}

.empty-logs {
  padding: 30px;
  color: #999;
}
</style>
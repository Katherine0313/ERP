<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  BellOutlined,
  CheckOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  LogoutOutlined,
  SettingOutlined,
  ShopOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { removeToken } from '@/utils/token.js'
import { getCurrentUser } from '@/utils/auth.js'

const props = defineProps({
  theme: { type: String, required: true },
  themePresets: { type: Array, required: true },
})
const emit = defineEmits(['update:theme'])
const router = useRouter()

// 界面设置抽屉与当前预设主题
const showSetting = ref(false)
const selectedTheme = computed({
  get: () => props.theme,
  set: value => emit('update:theme', value),
})

// 当前登录用户：token 里的 JWT payload，顶栏展示用户名和头像
const currentUser = computed(() => getCurrentUser() || {})
const username = computed(() => currentUser.value.username || '未登录')
const avatarUrl = computed(() => currentUser.value.adminAvatar || '')

// 全屏切换
const isFullscreen = ref(false)
function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}
document.addEventListener('fullscreenchange', () => {
  isFullscreen.value = !!document.fullscreenElement
})

// 用户下拉菜单
const menuClick = ({ key }) => {
  if (key === 'profile') {
    router.push({ name: 'Profile' })
  } else if (key === 'logout') {
    removeToken()
    router.push('/login')
  }
}
</script>

<template>
  <!-- 结构对齐 e-commerce 的顶栏：左 logo + 系统名 / 中间 main / 右 操作图标 + 头像下拉 -->
  <div class="header">
    <div class="header-left">
      <div class="logo">
        <span class="logo-icon">
          <ShopOutlined />
        </span>
        <span class="system-name">ERP 管理系统</span>
      </div>
    </div>

    <div class="header-main"></div>

    <div class="header-right">
      <div class="header-action" title="消息通知">
        <BellOutlined />
      </div>
      <div class="header-action" title="设置" @click="showSetting = true">
        <SettingOutlined />
      </div>
      <div class="header-action" :title="isFullscreen ? '退出全屏' : '全屏'" @click="toggleFullscreen">
        <FullscreenExitOutlined v-if="isFullscreen" />
        <FullscreenOutlined v-else />
      </div>

      <a-dropdown placement="bottomRight">
        <div class="personal">
          <a-avatar :size="24" :src="avatarUrl || undefined">
            <template #icon><UserOutlined /></template>
          </a-avatar>
          <span class="username">{{ username }}</span>
        </div>
        <template #overlay>
          <a-menu @click="menuClick">
            <a-menu-item key="profile">
              <UserOutlined />
              个人中心
            </a-menu-item>
            <a-menu-item key="logout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </div>

  <a-drawer v-model:visible="showSetting" title="界面设置" placement="right" width="360">
    <div class="setting-title">预设主题</div>
    <div class="theme-list">
      <div
          v-for="item in themePresets"
          :key="item.key"
          class="theme-item"
          @click="selectedTheme = item.key"
      >
        <div class="theme-preview" :class="{ active: selectedTheme === item.key }">
          <span class="preview-header" :style="{ background: item.headerColor }"></span>
          <span class="preview-sider" :style="{ background: item.siderColor }"></span>
        </div>
        <div class="theme-name">
          <span>{{ item.title }}</span>
          <CheckOutlined v-if="selectedTheme === item.key" />
        </div>
      </div>
    </div>
  </a-drawer>
</template>

<style scoped>
/* 高度、间距、图标尺寸对齐 e-commerce 的 stepin-header（--height-header: 56px） */
.header {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 16px;
  color: #fff;
  background: var(--erp-header-bg);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

/* logo：图标方块 + 系统名 */
.logo {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 12px;
  margin-left: -16px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  font-size: 16px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.2);
}

.system-name {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-main {
  flex: 1 1 0%;
}

/* 右侧操作区 */
.header-right {
  display: flex;
  align-items: center;
  height: 100%;
}

/* 操作图标：整块可点，hover 有背景 */
.header-action {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 8px 12px;
  font-size: 18px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.header-action:hover {
  background-color: rgba(255, 255, 255, 0.18);
}

/* 头像 + 用户名 */
.personal {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 8px 12px;
  margin-left: 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.personal:hover {
  background-color: rgba(255, 255, 255, 0.18);
}

.username {
  margin-left: 8px;
  font-size: 14px;
  white-space: nowrap;
}

.setting-title {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}

.theme-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.theme-item {
  cursor: pointer;
}

.theme-preview {
  position: relative;
  height: 64px;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.theme-preview.active {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.preview-header {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 16px;
}

.preview-sider {
  position: absolute;
  top: 16px;
  bottom: 0;
  left: 0;
  width: 32px;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

.theme-name {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.65);
}
</style>

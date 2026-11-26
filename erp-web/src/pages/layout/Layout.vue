<script setup>
import { computed, ref, watch } from 'vue'
import Header from './Header.vue'
import SideMenu from '@/pages/layout/SideMenu.vue'
import Footer from '@/pages/layout/Footer.vue'

const themePresets = [
  { key: 'light', title: '亮色模式', headerColor: '#1890ff', siderColor: '#ffffff', menuTheme: 'light' },
  { key: 'side-dark', title: '侧边暗色菜单', headerColor: '#1890ff', siderColor: '#001129', menuTheme: 'dark' },
  { key: 'header-dark', title: '顶部暗色菜单', headerColor: '#001129', siderColor: '#ffffff', menuTheme: 'light' },
  { key: 'vscode', title: 'VSCode风', headerColor: '#23272e', siderColor: '#181a1f', menuTheme: 'dark' },
  { key: 'idea', title: 'IDEA风', headerColor: '#2b2b2b', siderColor: '#3c3f41', menuTheme: 'dark' },
  { key: 'green', title: '墨绿风', headerColor: '#013a54', siderColor: '#012c40', menuTheme: 'dark' },
  { key: 'pink', title: '芭比粉', headerColor: '#b6266d', siderColor: '#8c1b52', menuTheme: 'dark' },
  { key: 'purple', title: '暗夜紫', headerColor: '#361f68', siderColor: '#27164a', menuTheme: 'dark' },
  { key: 'china', title: '中国红', headerColor: '#e60000', siderColor: '#b80000', menuTheme: 'dark' },
  { key: 'orange', title: '活力橙', headerColor: '#b1740d', siderColor: '#855a0a', menuTheme: 'dark' },
]

const savedTheme = localStorage.getItem('erp-layout-theme')
const selectedTheme = ref(themePresets.some(item => item.key === savedTheme) ? savedTheme : 'light')
const currentTheme = computed(() => themePresets.find(item => item.key === selectedTheme.value))

watch(selectedTheme, value => {
  localStorage.setItem('erp-layout-theme', value)
})
</script>

<template>
  <a-layout :style="{ '--erp-header-bg': currentTheme.headerColor }">
    <a-layout-header>
      <Header v-model:theme="selectedTheme" :theme-presets="themePresets" />
    </a-layout-header>
    <a-layout>
      <a-layout-sider width="200" :style="{ background: currentTheme.siderColor }">
        <SideMenu :menu-theme="currentTheme.menuTheme" />
      </a-layout-sider>
      <a-layout-content>
        <router-view />
      </a-layout-content>
    </a-layout>
    <a-layout-footer>
      <Footer />
    </a-layout-footer>
  </a-layout>

</template>

<style scoped>
:deep(.ant-layout-header){
  padding:0;
  background: none;
}

</style>
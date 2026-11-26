<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineProps({
  menuTheme: { type: String, default: 'light' },
})

const route = useRoute()
const router = useRouter()
const layoutRoute = router.options.routes.find(item => item.name === 'Layout')
const menuItems = (layoutRoute?.children || []).filter(item => item.meta?.showInMenu)
const selectedKeys = computed(() => [route.name])

const handleMenuClick = ({ key }) => {
  router.push({ name: key })
}
</script>

<template>
  <a-menu
      mode="inline"
      :theme="menuTheme"
      :selectedKeys="selectedKeys"
      style="background: transparent"
      @click="handleMenuClick"
  >
    <template v-for="item in menuItems" :key="item.name">
      <a-sub-menu v-if="item.children?.length" :key="item.name">
        <template #icon>
          <component :is="item.meta.icon" />
        </template>
        <template #title>{{ item.meta.title }}</template>
        <a-menu-item
            v-for="child in item.children.filter(child => child.meta?.showInMenu)"
            :key="child.name"
        >
          <component :is="child.meta.icon" />
          <span class="menu-title">{{ child.meta.title }}</span>
        </a-menu-item>
      </a-sub-menu>
      <a-menu-item v-else :key="item.name">
        <component :is="item.meta.icon" />
        <span class="menu-title">{{ item.meta.title }}</span>
      </a-menu-item>
    </template>
  </a-menu>
</template>

<style scoped>
.menu-title {
  margin-left: 2px;
}
</style>
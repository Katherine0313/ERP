<template>
  <div class="exp404">
    <a-result status="404" title="页面不存在" :sub-title="subTitle">
      <template #extra>
        <a-button type="primary" @click="goHome">返回首页</a-button>
        <a-button @click="goBack">返回上一页</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 把出错路径显示出来，方便用户截图反馈给管理员排查；过长时截断
const subTitle = computed(() => {
  const path = route.fullPath || route.path
  return path.length > 80 ? `找不到路径：${path.slice(0, 80)}…` : `找不到路径：${path}`
})

const goHome = () => {
  router.push('/')
}

// 返回上一页；没有上一页历史（直接输URL进来的）就回首页
const goBack = () => {
  if (router.options.history.state?.back) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.exp404 {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
</style>

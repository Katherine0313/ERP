import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path: '/',
    redirect: '/login',
  },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/pages/login/Login.vue'),
    },
    // 404 兜底，必须放在所有路由之后
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/pages/Exp404.vue'),
    },
  ],
})

export default router

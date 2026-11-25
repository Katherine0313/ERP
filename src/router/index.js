import { createRouter, createWebHistory, RouterView } from 'vue-router'
import {
  AppstoreOutlined,
  DeploymentUnitOutlined,
  HomeOutlined,
  ShopOutlined,
  ShoppingOutlined,
  TagsOutlined,
} from '@ant-design/icons-vue'

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
    {
      path: '/layout',
      name: 'Layout',
      redirect: '/layout/home',
      component: () => import('@/pages/layout/Layout.vue'),
      children: [
          {
            path: 'home',
            name: 'Home',
            meta: { title: '首页', icon: HomeOutlined, showInMenu: true },
            component: () => import('@/pages/home/Home.vue'),
          },
        {
          path: 'profile',
          name: 'Profile',
          meta: { title: '个人中心', showInMenu: false },
          component: () => import('@/pages/layout/Profile.vue'),
        },
        {
          path: 'product',
          name: 'Product',
          redirect: '/layout/product/product-category',
          component: RouterView,
          meta: { title: '商品管理', icon: ShopOutlined, showInMenu: true },
          children: [
            {
              path: 'product-category',
              name: 'ProductCategory',
              meta: { title: '商品类别', icon: AppstoreOutlined, showInMenu: true },
              component: () => import('@/pages/product/ProductCategoryList.vue'),
            },
            {
              path: 'product-info',
              name: 'ProductInfo',
              meta: { title: '商品信息', icon: ShoppingOutlined, showInMenu: true },
              component: () => import('@/pages/product/ProductInfoList.vue'),
            },
            {
              path: 'product-unit',
              name: 'ProductUnit',
              meta: { title: '多单位', icon: DeploymentUnitOutlined, showInMenu: true },
              component: () => import('@/pages/product/ProductUnit.vue'),
            },
            {
              path: 'product-attribute-group',
              name: 'ProductAttributeGroup',
              meta: { title: '商品属性', icon: TagsOutlined, showInMenu: true },
              component: () => import('@/pages/product/ProductAttributeGroupList.vue'),
            },
            {
              path: 'product-attribute-value',
              name: 'ProductAttributeValue',
              meta: { title: '多属性', icon: AppstoreOutlined, showInMenu: true },
              component: () => import('@/pages/product/ProductAttributeValueList.vue'),
            },
          ],
        }
      ]
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

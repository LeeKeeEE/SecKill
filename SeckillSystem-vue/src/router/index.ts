import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'activity-list',
      component: HomeView,
    },
    {
      path: '/activity/:id',
      name: 'activity-detail',
      component: () => import('../views/ActivityDetailView.vue'),
    },
    {
      path: '/orders',
      name: 'order-list',
      component: () => import('../views/OrderListView.vue') // 懒加载
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue') // 懒加载
    }
  ],
})

export default router

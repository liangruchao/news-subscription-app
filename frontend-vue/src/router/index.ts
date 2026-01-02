import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true, title: '首页' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true, title: '注册' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心' },
  },
  {
    path: '/preferences',
    name: 'Preferences',
    component: () => import('@/views/Preferences.vue'),
    meta: { requiresAuth: true, title: '偏好设置' },
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true, title: '消息中心' },
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('@/views/Announcements.vue'),
    meta: { title: '系统公告' },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 全局前置守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 新闻订阅系统`
  }

  // 需要认证的路由
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next({
        name: 'Login',
        query: { redirect: to.fullPath },
      })
      return
    }
  }

  // 游客路由（已登录用户不能访问）
  if (to.meta.guest && authStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router

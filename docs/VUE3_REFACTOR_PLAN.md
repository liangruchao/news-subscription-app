# Vue 3 前端重构方案

## 一、技术栈

### 核心技术
- **Vue 3.4+**: 使用 Composition API
- **Vite 5.x**: 构建工具，快速热更新
- **Vue Router 4.x**: 单页应用路由
- **Pinia 2.x**: 状态管理（Vue 3 官方推荐）
- **Element Plus**: UI 组件库
- **Axios**: HTTP 客户端
- **Vue I18n 9.x**: 国际化

### 开发工具
- **ESLint**: 代码检查
- **Prettier**: 代码格式化
- **SCSS**: CSS 预处理器

---

## 二、项目结构

```
frontend-vue/
├── public/
│   └── favicon.ico
├── src/
│   ├── assets/              # 静态资源
│   │   ├── images/
│   │   └── styles/
│   ├── components/          # 公共组件
│   │   ├── common/
│   │   │   ├── AppHeader.vue      # 顶部导航
│   │   │   ├── AppFooter.vue      # 页脚
│   │   │   ├── MessageAlert.vue   # 消息提示
│   │   │   └── LoadingSpinner.vue # 加载动画
│   │   ├── news/
│   │   │   ├── NewsCard.vue       # 新闻卡片
│   │   │   └── CategorySelector.vue
│   │   └── user/
│   │       ├── UserAvatar.vue
│   │       └── PasswordForm.vue
│   ├── composables/         # Composition API 复用逻辑
│   │   ├── useAuth.js              # 认证逻辑
│   │   ├── useNews.js              # 新闻逻辑
│   │   ├── useMessage.js           # 消息逻辑
│   │   └── useLocalStorage.js      # 本地存储
│   ├── views/                # 页面组件
│   │   ├── Home.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Profile.vue
│   │   ├── Preferences.vue
│   │   ├── Messages.vue
│   │   └── Announcements.vue
│   ├── router/               # 路由
│   │   └── index.js
│   ├── stores/               # Pinia 状态管理
│   │   ├── auth.js           # 用户认证状态
│   │   ├── news.js           # 新闻状态
│   │   └── ui.js             # UI 状态（loading, message等）
│   ├── api/                  # API 接口封装
│   │   ├── request.js        # Axios 实例配置
│   │   ├── auth.js
│   │   ├── news.js
│   │   ├── user.js
│   │   └── message.js
│   ├── utils/                # 工具函数
│   │   ├── format.js         # 时间、数字格式化
│   │   ├── validate.js       # 表单验证
│   │   └── constants.js      # 常量
│   ├── i18n/                 # 国际化
│   │   ├── index.js
│   │   ├── locales/
│   │   │   ├── zh-CN.js
│   │   │   └── en-US.js
│   ├── styles/               # 全局样式
│   │   ├── variables.scss    # SCSS 变量
│   │   ├── mixins.scss       # SCSS mixins
│   │   └── global.scss       # 全局样式
│   ├── App.vue               # 根组件
│   └── main.js               # 入口文件
├── index.html
├── vite.config.js
├── .eslintrc.cjs
├── .prettierrc
└── package.json
```

---

## 三、核心实现代码

### 3.1 Vite 配置 (`vite.config.js`)

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
```

### 3.2 Axios 请求封装 (`src/api/request.js`)

```javascript
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data
    if (code === 200 || response.data.success) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未登录，请先登录')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
```

### 3.3 Auth Store (`src/stores/auth.js`)

```javascript
import { defineStore } from 'pinia'
import { loginApi, registerApi, getCurrentUserApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    isLoggedIn: !!localStorage.getItem('token'),
  }),

  getters: {
    userId: (state) => state.user?.id,
    username: (state) => state.user?.username,
    email: (state) => state.user?.email,
  },

  actions: {
    // 登录
    async login(credentials) {
      try {
        const data = await loginApi(credentials)
        this.token = data.token
        this.user = data.user
        this.isLoggedIn = true

        // 保存到 localStorage
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data.user))

        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 注册
    async register(userInfo) {
      try {
        const data = await registerApi(userInfo)
        this.token = data.token
        this.user = data.user
        this.isLoggedIn = true

        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data.user))

        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 登出
    logout() {
      this.token = ''
      this.user = null
      this.isLoggedIn = false

      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    // 获取当前用户信息
    async fetchCurrentUser() {
      try {
        const user = await getCurrentUserApi()
        this.user = user
        localStorage.setItem('user', JSON.stringify(user))
        return user
      } catch (error) {
        this.logout()
        throw error
      }
    },
  },
})
```

### 3.4 useAuth Composable (`src/composables/useAuth.js`)

```javascript
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()

  const isLoggedIn = computed(() => authStore.isLoggedIn)
  const user = computed(() => authStore.user)
  const userId = computed(() => authStore.userId)
  const username = computed(() => authStore.username)

  const login = async (credentials) => {
    const result = await authStore.login(credentials)
    if (result.success) {
      router.push('/')
    }
    return result
  }

  const logout = () => {
    authStore.logout()
    router.push('/login')
  }

  const checkAuth = () => {
    if (!isLoggedIn.value) {
      router.push('/login')
    }
  }

  return {
    isLoggedIn,
    user,
    userId,
    username,
    login,
    logout,
    checkAuth,
  }
}
```

### 3.5 路由配置 (`src/router/index.js`)

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/preferences',
    name: 'Preferences',
    component: () => import('@/views/Preferences.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('@/views/Announcements.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.guest && authStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
```

### 3.6 主入口 (`src/main.js`)

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import enUs from 'element-plus/es/locale/lang/en'

import App from './App.vue'
import router from './router'
import i18n from './i18n'
import './styles/global.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

### 3.7 示例：Login 页面 (`src/views/Login.vue`)

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>{{ t('auth.login') }}</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item :label="t('auth.username')" prop="username">
          <el-input
            v-model="form.username"
            :placeholder="t('auth.usernameRequired')"
            clearable
          />
        </el-form-item>

        <el-form-item :label="t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t('auth.passwordRequired')"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.rememberMe">
            {{ t('auth.rememberMe') }}
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            {{ t('auth.login') }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-link type="primary" @click="$router.push('/register')">
            {{ t('auth.noAccount') }}
          </el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuth } from '@/composables/useAuth'
import { validateUsername, validatePassword } from '@/utils/validate'

const { t } = useI18n()
const { login } = useAuth()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

const rules = {
  username: [
    { required: true, message: () => t('auth.usernameRequired'), trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const result = await login({
    username: form.username,
    password: form.password,
  })
  loading.value = false

  if (!result.success) {
    ElMessage.error(result.message)
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

h2 {
  margin: 0;
  text-align: center;
  color: #333;
}
</style>
```

---

## 四、重构策略

### 方案 A：渐进式重构（推荐）

**阶段 1：搭建 Vue 3 项目（1 周）**
- 初始化 Vite + Vue 3 项目
- 配置 Vue Router、Pinia、Element Plus
- 搭建基础布局和路由

**阶段 2：迁移核心页面（2 周）**
- 优先迁移：Login → Register → Home → Profile
- 保持后端 API 不变
- 新旧前端并行运行

**阶段 3：迁移剩余页面（1 周）**
- 迁移：Preferences → Messages → Announcements
- 完善功能测试

**阶段 4：优化与部署（1 周）**
- 性能优化
- 响应式适配
- 生产环境部署

### 方案 B：全量重构

直接创建全新的 Vue 3 项目，一次性完成所有页面开发。

---

## 五、优势对比

| 特性 | 当前 Vanilla JS | Vue 3 |
|------|-----------------|-------|
| 开发效率 | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 代码维护 | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 组件复用 | ⭐ | ⭐⭐⭐⭐⭐ |
| 状态管理 | ⭐ | ⭐⭐⭐⭐⭐ |
| 路由管理 | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 生态系统 | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 学习曲线 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 性能 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## 六、后端适配（可选）

如果希望同时支持 Session 和 JWT 认证：

### 6.1 新增 JWT 登录接口

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    /**
     * JWT 登录（新增）
     */
    @PostMapping("/jwt/login")
    public ApiResponse<JwtResponse> jwtLogin(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        String token = jwtUtil.generateToken(user.getId());
        return ApiResponse.success(JwtResponse.builder()
            .token(token)
            .user(user)
            .build());
    }
}
```

---

## 七、下一步建议

1. **确认重构方式**：渐进式 vs 全量重构
2. **准备开发环境**：安装 Node.js 18+, pnpm/npm
3. **初始化项目**：使用 Vite 创建 Vue 3 项目
4. **配置开发工具**：VS Code + Volar 插件

需要我开始创建 Vue 3 项目吗？

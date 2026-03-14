import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { useAuthStore } from '@/stores/auth'
import '@/assets/styles/global.scss'

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

// 初始化认证状态
const authStore = useAuthStore()
authStore.initAuth()

// 验证 session 是否有效（在应用挂载后）
authStore.fetchCurrentUser().catch(() => {
  // session 无效，fetchCurrentUser 会自动调用 logout 清除状态
  console.log('Session 已过期或未登录')
})

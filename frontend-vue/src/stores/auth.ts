import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginRequest, RegisterRequest } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string>('')
  const isLoggedIn = computed(() => !!user.value)

  // 初始化：从 localStorage 恢复数据
  const initAuth = () => {
    const savedUser = localStorage.getItem('user')
    const savedToken = localStorage.getItem('token')

    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        user.value = null
      }
    }

    if (savedToken) {
      token.value = savedToken
    }
  }

  // Actions
  const setUser = (userData: User) => {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const setToken = (tokenValue: string) => {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
  }

  const login = async (credentials: LoginRequest) => {
    try {
      const user = await authApi.loginApi(credentials)
      setUser(user)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  const register = async (userInfo: RegisterRequest) => {
    try {
      const user = await authApi.registerApi(userInfo)
      setUser(user)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  const logout = async () => {
    try {
      await authApi.logoutApi()
    } catch {
      // 忽略登出 API 错误
    } finally {
      user.value = null
      token.value = ''
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      // 跳转到登录页（如果当前不在登录页）
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
  }

  const fetchCurrentUser = async () => {
    try {
      const userData = await authApi.getCurrentUserApi()
      setUser(userData)
      return userData
    } catch (error) {
      // 获取用户信息失败，清除登录状态
      await logout()
      throw error
    }
  }

  // Getters
  const userId = computed(() => user.value?.id)
  const username = computed(() => user.value?.username)
  const email = computed(() => user.value?.email)
  const avatarUrl = computed(() => user.value?.avatarUrl)

  return {
    // State
    user,
    token,
    isLoggedIn,
    // Getters
    userId,
    username,
    email,
    avatarUrl,
    // Actions
    initAuth,
    setUser,
    setToken,
    login,
    register,
    logout,
    fetchCurrentUser,
  }
})

import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from './types'

// 创建 Axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 支持 Session 认证（携带 Cookie）
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 获取 token（如果使用 JWT）
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse

    // 后端返回的 ApiResponse 格式：{ success: boolean, data: any, message?: string }
    if (data.success === true || data.code === 200) {
      return data.data
    } else {
      // 业务错误
      const errorMsg = data.message || '请求失败'
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
  },
  (error: AxiosError) => {
    // HTTP 错误
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as ApiResponse

      switch (status) {
        case 401:
          ElMessage.error(data.message || '未登录，请先登录')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error(data.message || '没有权限访问')
          break
        case 404:
          ElMessage.error(data.message || '请求的资源不存在')
          break
        case 500:
          ElMessage.error(data.message || '服务器错误')
          break
        default:
          ElMessage.error(data.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络错误，请稍后重试')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default request

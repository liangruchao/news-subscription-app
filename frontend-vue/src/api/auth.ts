import request from './request'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from './types'

/**
 * 用户登录
 */
export const loginApi = (data: LoginRequest) => {
  return request.post<any, LoginResponse>('/auth/login', data)
}

/**
 * 用户注册
 */
export const registerApi = (data: RegisterRequest) => {
  return request.post<any, LoginResponse>('/auth/register', data)
}

/**
 * 用户登出
 */
export const logoutApi = () => {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export const getCurrentUserApi = () => {
  return request.get<any, User>('/auth/current')
}

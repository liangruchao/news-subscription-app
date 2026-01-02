// API 响应类型
export interface ApiResponse<T = any> {
  success: boolean
  code?: number
  message?: string
  data: T
}

// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  avatarUrl?: string
  bio?: string
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface LoginResponse {
  user: User
  token?: string
}

// 新闻相关类型
export interface NewsItem {
  title: string
  description: string
  url: string
  urlToImage?: string
  publishedAt: string
  source?: {
    name: string
  }
  content?: string
}

export interface Subscription {
  id: number
  userId: number
  category: string
  createdAt: string
}

// 消息相关类型
export interface Message {
  id: number
  userId: number
  title: string
  content: string
  type: 'system' | 'subscription' | 'profile' | 'announcement'
  isRead: boolean
  createdAt: string
}

// 公告相关类型
export interface Announcement {
  id: number
  title: string
  content: string
  priority: 'low' | 'medium' | 'high'
  isPublished: boolean
  createdAt: string
  updatedAt: string
}

// 统计相关类型
export interface UserStats {
  totalSubscriptions: number
  totalMessages: number
  unreadMessages: number
  loginDays: number
}

// 偏好设置类型
export interface UserPreferences {
  newsNotification: boolean
  systemNotification: boolean
  subscriptionNotification: boolean
  newsPageSize: number
  compactMode: boolean
  interfaceLanguage: string
  publicProfile: boolean
  showOnlineStatus: boolean
}

// 后端返回的UserPreference类型（字段名不同）
export interface UserPreferenceDTO {
  newsNotificationEnabled: boolean
  systemNotificationEnabled: boolean
  subscriptionNotificationEnabled: boolean
  newsPageSize: number
  compactMode: boolean
  language: string
  publicProfile: boolean
  showOnlineStatus: boolean
}

// 登录历史类型
export interface LoginHistory {
  id: number
  userId: number
  ipAddress: string
  userAgent?: string
  loginTime: string
  loginSuccess: boolean
}

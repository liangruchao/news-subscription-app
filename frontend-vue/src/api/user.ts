import request from './request'
import type { User, UserPreferences, UserStats, UserPreferenceDTO } from './types'

/**
 * 获取用户资料
 */
export const getUserProfileApi = () => {
  return request.get<any, User>('/profile')
}

/**
 * 更新用户资料
 */
export const updateUserProfileApi = (data: Partial<User>) => {
  return request.put('/profile', data)
}

/**
 * 修改密码
 */
export const changePasswordApi = (data: { oldPassword: string; newPassword: string }) => {
  return request.put('/profile/password', data)
}

/**
 * 上传头像
 */
export const uploadAvatarApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, { avatarUrl: string }>('/profile/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 获取用户统计信息
 */
export const getUserStatsApi = () => {
  return request.get<any, UserStats>('/stats')
}

/**
 * 获取用户偏好设置
 */
export const getUserPreferencesApi = async (): Promise<UserPreferences> => {
  const dto = await request.get<any, UserPreferenceDTO>('/preferences')
  // 将后端DTO映射到前端格式
  return {
    newsNotification: dto.newsNotificationEnabled,
    systemNotification: dto.systemNotificationEnabled,
    subscriptionNotification: dto.subscriptionNotificationEnabled,
    newsPageSize: dto.newsPageSize,
    compactMode: dto.compactMode,
    interfaceLanguage: dto.language,
    publicProfile: dto.publicProfile,
    showOnlineStatus: dto.showOnlineStatus,
  }
}

/**
 * 更新用户偏好设置
 */
export const updateUserPreferencesApi = async (data: UserPreferences): Promise<void> => {
  // 将前端格式映射到后端DTO
  const dto: Record<string, any> = {
    newsNotificationEnabled: data.newsNotification,
    systemNotificationEnabled: data.systemNotification,
    subscriptionNotificationEnabled: data.subscriptionNotification,
    newsPageSize: data.newsPageSize,
    compactMode: data.compactMode,
    language: data.interfaceLanguage,
    publicProfile: data.publicProfile,
    showOnlineStatus: data.showOnlineStatus,
  }
  return request.put('/preferences', dto)
}

/**
 * 注销账户
 */
export const deleteAccountApi = () => {
  return request.delete('/profile')
}

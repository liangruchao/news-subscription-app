import request from './request'
import type { Message, Announcement } from './types'

/**
 * 获取所有消息
 */
export const getMessagesApi = () => {
  return request.get<any, Message[]>('/messages')
}

/**
 * 获取未读消息
 */
export const getUnreadMessagesApi = () => {
  return request.get<any, Message[]>('/messages/unread')
}

/**
 * 获取未读消息数量
 */
export const getUnreadCountApi = () => {
  return request.get<any, { count: number }>('/messages/unread/count')
}

/**
 * 标记消息为已读
 */
export const markAsReadApi = (id: number) => {
  return request.put(`/messages/${id}/read`)
}

/**
 * 标记所有消息为已读
 */
export const markAllAsReadApi = () => {
  return request.put('/messages/read-all')
}

/**
 * 删除消息
 */
export const deleteMessageApi = (id: number) => {
  return request.delete(`/messages/${id}`)
}

/**
 * 获取所有公告
 */
export const getAnnouncementsApi = () => {
  return request.get<any, Announcement[]>('/announcements')
}

/**
 * 获取公告详情
 */
export const getAnnouncementDetailApi = (id: number) => {
  return request.get<any, Announcement>(`/announcements/${id}`)
}

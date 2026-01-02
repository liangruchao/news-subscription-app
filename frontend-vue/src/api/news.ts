import request from './request'
import type { NewsItem, Subscription } from './types'

/**
 * 获取用户订阅的新闻
 */
export const getUserNewsApi = () => {
  return request.get<any, NewsItem[]>('/news')
}

/**
 * 获取指定类别的新闻
 */
export const getNewsByCategoryApi = (category: string) => {
  return request.get<any, NewsItem[]>(`/news/category/${category}`)
}

/**
 * 获取用户订阅列表
 */
export const getSubscriptionsApi = () => {
  return request.get<any, Subscription[]>('/subscriptions')
}

/**
 * 添加订阅
 */
export const addSubscriptionApi = (category: string) => {
  return request.post('/subscriptions', { category })
}

/**
 * 取消订阅
 */
export const removeSubscriptionApi = (category: string) => {
  return request.delete(`/subscriptions/${category}`)
}

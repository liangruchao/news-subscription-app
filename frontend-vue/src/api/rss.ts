import request from './request'
import type { ApiResponse, RssFeed, Article, ArticleDetail, UserRssSubscription, UserFavorite, UserFavoriteStats, ArticleShare, ShareArticleRequest } from './types'

// ==================== RSS源管理 ====================

/**
 * 获取所有激活的RSS源
 */
export function getActiveRssFeeds(): Promise<ApiResponse<RssFeed[]>> {
  return request.get<any, ApiResponse<RssFeed[]>>('/rss/feeds/active')
}

/**
 * 根据分类获取RSS源
 */
export function getRssFeedsByCategory(category: string): Promise<ApiResponse<RssFeed[]>> {
  return request.get<any, ApiResponse<RssFeed[]>>(`/rss/feeds/category/${category}`)
}

/**
 * 搜索RSS源
 */
export function searchRssFeeds(keyword: string): Promise<ApiResponse<RssFeed[]>> {
  return request.get<any, ApiResponse<RssFeed[]>>('/rss/feeds/search', { params: { keyword } })
}

/**
 * 获取所有分类
 */
export function getRssCategories(): Promise<ApiResponse<string[]>> {
  return request.get<any, ApiResponse<string[]>>('/rss/feeds/categories')
}

/**
 * 添加RSS源（需要登录）
 */
export function addRssFeed(data: {
  url: string
  title?: string
  description?: string
  category?: string
  language?: string
  iconUrl?: string
}): Promise<ApiResponse<RssFeed>> {
  return request.post<any, ApiResponse<RssFeed>>('/rss/feeds', data)
}

/**
 * 更新RSS源（需要登录）
 */
export function updateRssFeed(id: number, data: Partial<RssFeed>): Promise<ApiResponse<RssFeed>> {
  return request.put<any, ApiResponse<RssFeed>>(`/rss/feeds/${id}`, data)
}

/**
 * 删除RSS源（需要登录）
 */
export function deleteRssFeed(id: number): Promise<ApiResponse<void>> {
  return request.delete<any, ApiResponse<void>>(`/rss/feeds/${id}`)
}

/**
 * 激活/禁用RSS源（需要登录）
 */
export function toggleRssFeedActive(id: number, active: boolean): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/rss/feeds/${id}/toggle`, null, {
    params: { active }
  })
}

/**
 * 手动触发RSS源抓取（需要登录）
 */
export function fetchRssFeed(id: number): Promise<ApiResponse<number>> {
  return request.post<any, ApiResponse<number>>(`/rss/feeds/${id}/fetch`)
}

/**
 * 手动触发所有RSS源抓取（需要登录）
 */
export function fetchAllRssFeeds(): Promise<ApiResponse<number>> {
  return request.post<any, ApiResponse<number>>('/rss/feeds/fetch-all')
}

// ==================== 用户订阅管理 ====================

/**
 * 获取用户的订阅列表（需要登录）
 */
export function getUserSubscriptions(): Promise<ApiResponse<RssFeed[]>> {
  return request.get<any, ApiResponse<RssFeed[]>>('/rss/feeds/subscriptions')
}

/**
 * 检查用户是否订阅了指定RSS源（需要登录）
 */
export function checkSubscription(feedId: number): Promise<ApiResponse<boolean>> {
  return request.get<any, ApiResponse<boolean>>(`/rss/feeds/${feedId}/subscription/status`)
}

/**
 * 订阅RSS源（需要登录）
 */
export function subscribeToFeed(feedId: number, customTitle?: string): Promise<ApiResponse<UserRssSubscription>> {
  return request.post<any, ApiResponse<UserRssSubscription>>(`/rss/feeds/${feedId}/subscribe`, null, {
    params: { customTitle }
  })
}

/**
 * 取消订阅RSS源（需要登录）
 */
export function unsubscribeFromFeed(feedId: number): Promise<ApiResponse<void>> {
  return request.delete<any, ApiResponse<void>>(`/rss/feeds/${feedId}/subscribe`)
}

/**
 * 切换收藏状态（需要登录）
 */
export function toggleFeedFavorite(feedId: number): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/rss/feeds/${feedId}/favorite`)
}

/**
 * 获取用户收藏的RSS源（需要登录）
 */
export function getUserFavoriteFeeds(): Promise<ApiResponse<RssFeed[]>> {
  return request.get<any, ApiResponse<RssFeed[]>>('/rss/feeds/subscriptions/favorites')
}

/**
 * 更新订阅优先级（需要登录）
 */
export function updateSubscriptionPriority(feedId: number, priority: number): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/rss/feeds/${feedId}/subscription/priority`, null, {
    params: { priority }
  })
}

/**
 * 更新自定义标题（需要登录）
 */
export function updateSubscriptionCustomTitle(feedId: number, customTitle: string): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/rss/feeds/${feedId}/subscription/title`, null, {
    params: { customTitle }
  })
}

// ==================== 文章管理 ====================

/**
 * 获取用户订阅的文章流（需要登录）
 */
export function getUserArticles(limit: number = 20): Promise<ApiResponse<Article[]>> {
  return request.get<any, ApiResponse<Article[]>>('/articles/feed', { params: { limit } })
}

/**
 * 获取指定RSS源的文章（分页）
 */
export function getArticlesByFeed(feedId: number, page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: Article[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>(`/articles/feed/${feedId}`, {
    params: { page, size }
  })
}

/**
 * 获取指定分类的文章（分页）
 */
export function getArticlesByCategory(category: string, page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: Article[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>(`/articles/category/${category}`, {
    params: { page, size }
  })
}

/**
 * 获取文章详情
 */
export function getArticleDetail(articleId: number): Promise<ApiResponse<ArticleDetail>> {
  return request.get<any, ApiResponse<ArticleDetail>>(`/articles/${articleId}`)
}

/**
 * 搜索文章
 */
export function searchArticles(keyword: string, page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: Article[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>('/articles/search', {
    params: { keyword, page, size }
  })
}

/**
 * 获取热门文章
 */
export function getPopularArticles(days: number = 7, limit: number = 10): Promise<ApiResponse<Article[]>> {
  return request.get<any, ApiResponse<Article[]>>('/articles/popular', {
    params: { days, limit }
  })
}

/**
 * 获取最新文章
 */
export function getLatestArticles(limit: number = 10): Promise<ApiResponse<Article[]>> {
  return request.get<any, ApiResponse<Article[]>>('/articles/latest', {
    params: { limit }
  })
}

// ==================== 用户收藏管理 ====================

/**
 * 获取用户收藏列表（分页）
 */
export function getUserFavorites(page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: UserFavorite[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>('/favorites', {
    params: { page, size }
  })
}

/**
 * 获取用户所有收藏
 */
export function getAllUserFavorites(): Promise<ApiResponse<UserFavorite[]>> {
  return request.get<any, ApiResponse<UserFavorite[]>>('/favorites/all')
}

/**
 * 根据标签筛选收藏
 */
export function getFavoritesByTag(tag: string, page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: UserFavorite[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>(`/favorites/tag/${tag}`, {
    params: { page, size }
  })
}

/**
 * 获取未读收藏
 */
export function getUnreadFavorites(page: number = 0, size: number = 20): Promise<ApiResponse<{
  content: UserFavorite[]
  totalElements: number
  totalPages: number
}>> {
  return request.get<any, ApiResponse<any>>('/favorites/unread', {
    params: { page, size }
  })
}

/**
 * 检查是否收藏了指定文章
 */
export function checkFavorite(articleId: number): Promise<ApiResponse<boolean>> {
  return request.get<any, ApiResponse<boolean>>(`/favorites/check/${articleId}`)
}

/**
 * 收藏文章
 */
export function addFavorite(articleId: number, notes?: string, tags?: string): Promise<ApiResponse<UserFavorite>> {
  return request.post<any, ApiResponse<UserFavorite>>(`/favorites/${articleId}`, null, {
    params: { notes, tags }
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(articleId: number): Promise<ApiResponse<void>> {
  return request.delete<any, ApiResponse<void>>(`/favorites/${articleId}`)
}

/**
 * 更新收藏笔记
 */
export function updateFavoriteNotes(articleId: number, notes: string): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/favorites/${articleId}/notes`, null, {
    params: { notes }
  })
}

/**
 * 更新收藏标签
 */
export function updateFavoriteTags(articleId: number, tags: string): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/favorites/${articleId}/tags`, null, {
    params: { tags }
  })
}

/**
 * 切换已读状态
 */
export function toggleReadStatus(articleId: number): Promise<ApiResponse<void>> {
  return request.patch<any, ApiResponse<void>>(`/favorites/${articleId}/read`)
}

/**
 * 标记为已读
 */
export function markAsRead(articleId: number): Promise<ApiResponse<void>> {
  return request.post<any, ApiResponse<void>>(`/favorites/${articleId}/read`)
}

/**
 * 获取用户的所有标签
 */
export function getUserTags(): Promise<ApiResponse<string[]>> {
  return request.get<any, ApiResponse<string[]>>('/favorites/tags')
}

/**
 * 获取用户收藏统计
 */
export function getUserStats(): Promise<ApiResponse<UserFavoriteStats>> {
  return request.get<any, ApiResponse<UserFavoriteStats>>('/favorites/stats')
}

// ==================== 文章分享管理 ====================

/**
 * 创建文章分享（需要登录）
 */
export function createShare(data: ShareArticleRequest): Promise<ArticleShare> {
  return request.post<any, ArticleShare>('/shares', data)
}

/**
 * 获取用户的分享列表（需要登录）
 */
export function getUserShares(): Promise<ArticleShare[]> {
  return request.get<any, ArticleShare[]>('/shares')
}

/**
 * 删除分享（需要登录）
 */
export function deleteShare(id: number): Promise<void> {
  return request.delete<any, void>(`/shares/${id}`)
}

/**
 * 根据分享码获取分享内容（公开访问）
 */
export function getShareByCode(shareCode: string): Promise<ArticleShare> {
  return request.get<any, ArticleShare>(`/shares/public/${shareCode}`)
}

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as rssApi from '@/api/rss'
import type { RssFeed, Article, ArticleDetail, UserFavorite, UserFavoriteStats } from '@/api/types'
import { ElMessage } from 'element-plus'

export const useRssStore = defineStore('rss', () => {
  // ==================== 状态 ====================

  // RSS源列表
  const feeds = ref<RssFeed[]>([])
  const feedsLoading = ref(false)

  // 用户订阅的RSS源
  const subscriptions = ref<RssFeed[]>([])
  const subscriptionsLoading = ref(false)

  // 用户收藏的RSS源
  const favoriteFeeds = ref<RssFeed[]>([])
  const favoriteFeedsLoading = ref(false)

  // 文章列表
  const articles = ref<Article[]>([])
  const articlesLoading = ref(false)
  const articlesTotal = ref(0)

  // 当前文章详情
  const currentArticle = ref<ArticleDetail | null>(null)
  const currentArticleLoading = ref(false)

  // 热门文章
  const popularArticles = ref<Article[]>([])

  // 最新文章
  const latestArticles = ref<Article[]>([])

  // 分类列表
  const categories = ref<string[]>([])

  // 搜索结果
  const searchResults = ref<Article[]>([])
  const searchTotal = ref(0)

  // ==================== 收藏相关状态 ====================

  // 用户收藏列表
  const favorites = ref<UserFavorite[]>([])
  const favoritesLoading = ref(false)
  const favoritesTotal = ref(0)

  // 用户标签列表
  const userTags = ref<string[]>([])
  const userTagsLoading = ref(false)

  // 用户收藏统计
  const userStats = ref<UserFavoriteStats | null>(null)

  // ==================== 计算属性 ====================

  // 订阅数量
  const subscriptionCount = computed(() => subscriptions.value.length)

  // 收藏数量
  const favoriteCount = computed(() => favoriteFeeds.value.length)

  // 按分类分组的RSS源
  const feedsByCategory = computed(() => {
    const grouped: Record<string, RssFeed[]> = {}
    for (const feed of feeds.value) {
      const category = feed.category || '未分类'
      if (!grouped[category]) {
        grouped[category] = []
      }
      grouped[category].push(feed)
    }
    return grouped
  })

  // 订阅的RSS源ID集合（用于快速查找）
  const subscribedFeedIds = computed(() => {
    return new Set(subscriptions.value.map(f => f.id))
  })

  // 检查是否订阅了指定RSS源
  const isSubscribed = (feedId: number) => {
    return subscribedFeedIds.value.has(feedId)
  }

  // ==================== RSS源管理方法 ====================

  /**
   * 获取所有激活的RSS源
   */
  async function fetchActiveFeeds() {
    feedsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      feeds.value = await rssApi.getActiveRssFeeds()
    } catch (error: any) {
      ElMessage.error(error.message || '获取RSS源列表失败')
    } finally {
      feedsLoading.value = false
    }
  }

  /**
   * 根据分类获取RSS源
   */
  async function fetchFeedsByCategory(category: string) {
    feedsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      feeds.value = await rssApi.getRssFeedsByCategory(category)
    } catch (error: any) {
      ElMessage.error(error.message || '获取RSS源失败')
    } finally {
      feedsLoading.value = false
    }
  }

  /**
   * 搜索RSS源
   */
  async function searchFeeds(keyword: string) {
    feedsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      feeds.value = await rssApi.searchRssFeeds(keyword)
    } catch (error: any) {
      ElMessage.error(error.message || '搜索RSS源失败')
    } finally {
      feedsLoading.value = false
    }
  }

  /**
   * 添加RSS源
   */
  async function addFeed(data: {
    url: string
    title?: string
    description?: string
    category?: string
    language?: string
    iconUrl?: string
  }) {
    try {
      // 响应拦截器已经返回 data.data
      const result = await rssApi.addRssFeed(data)
      ElMessage.success('RSS源添加成功')
      await fetchActiveFeeds()
      return result
    } catch (error: any) {
      ElMessage.error(error.message || '添加RSS源失败')
      throw error
    }
  }

  /**
   * 删除RSS源
   */
  async function deleteFeed(id: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.deleteRssFeed(id)
      ElMessage.success('RSS源删除成功')
      await fetchActiveFeeds()
    } catch (error: any) {
      ElMessage.error(error.message || '删除RSS源失败')
      throw error
    }
  }

  /**
   * 抓取RSS源
   */
  async function fetchFeed(id: number) {
    try {
      // 响应拦截器已经返回 data.data
      const count = await rssApi.fetchRssFeed(id)
      ElMessage.success(`抓取完成，新增 ${count} 篇文章`)
    } catch (error: any) {
      ElMessage.error(error.message || '抓取失败')
    }
  }

  // ==================== 订阅管理方法 ====================

  /**
   * 获取用户订阅列表
   */
  async function fetchSubscriptions() {
    subscriptionsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      subscriptions.value = await rssApi.getUserSubscriptions()
    } catch (error: any) {
      // 静默处理错误，用户可能未登录
      subscriptions.value = []
    } finally {
      subscriptionsLoading.value = false
    }
  }

  /**
   * 订阅RSS源
   */
  async function subscribe(feedId: number, customTitle?: string) {
    try {
      // 响应拦截器已经返回 data.data，操作成功时会返回数据或void
      await rssApi.subscribeToFeed(feedId, customTitle)
      ElMessage.success('订阅成功')
      await fetchSubscriptions()
    } catch (error: any) {
      ElMessage.error(error.message || '订阅失败')
      throw error
    }
  }

  /**
   * 取消订阅
   */
  async function unsubscribe(feedId: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.unsubscribeFromFeed(feedId)
      ElMessage.success('取消订阅成功')
      await fetchSubscriptions()
    } catch (error: any) {
      ElMessage.error(error.message || '取消订阅失败')
      throw error
    }
  }

  /**
   * 切换收藏状态
   */
  async function toggleFavorite(feedId: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.toggleFeedFavorite(feedId)
      await fetchSubscriptions()
      await fetchFavoriteFeeds()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  }

  /**
   * 获取用户收藏列表
   */
  async function fetchFavoriteFeeds() {
    favoriteFeedsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      favoriteFeeds.value = await rssApi.getUserFavoriteFeeds()
    } catch (error: any) {
      ElMessage.error(error.message || '获取收藏列表失败')
    } finally {
      favoriteFeedsLoading.value = false
    }
  }

  // ==================== 文章管理方法 ====================

  /**
   * 获取用户文章流
   */
  async function fetchUserArticles(limit: number = 20) {
    articlesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      const result = await rssApi.getUserArticles(limit)
      articles.value = result
      articlesTotal.value = result.length
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章失败')
    } finally {
      articlesLoading.value = false
    }
  }

  /**
   * 获取指定RSS源的文章
   */
  async function fetchArticlesByFeed(feedId: number, page: number = 0) {
    articlesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data，这是分页对象
      const result = await rssApi.getArticlesByFeed(feedId, page)
      articles.value = result.content
      articlesTotal.value = result.totalElements
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章失败')
    } finally {
      articlesLoading.value = false
    }
  }

  /**
   * 获取文章详情
   */
  async function fetchArticleDetail(articleId: number) {
    currentArticleLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      const result = await rssApi.getArticleDetail(articleId)
      currentArticle.value = result
      return result
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章详情失败')
      throw error
    } finally {
      currentArticleLoading.value = false
    }
  }

  /**
   * 搜索文章
   */
  async function searchArticles(keyword: string, page: number = 0) {
    articlesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data，这是分页对象
      const result = await rssApi.searchArticles(keyword, page)
      searchResults.value = result.content
      searchTotal.value = result.totalElements
      articles.value = result.content
      articlesTotal.value = result.totalElements
    } catch (error: any) {
      ElMessage.error(error.message || '搜索失败')
    } finally {
      articlesLoading.value = false
    }
  }

  /**
   * 获取热门文章
   */
  async function fetchPopularArticles(days: number = 7, limit: number = 10) {
    try {
      // 响应拦截器已经返回 data.data
      popularArticles.value = await rssApi.getPopularArticles(days, limit)
    } catch (error: any) {
      console.error('获取热门文章失败:', error)
    }
  }

  /**
   * 获取最新文章
   */
  async function fetchLatestArticles(limit: number = 10) {
    try {
      // 响应拦截器已经返回 data.data
      latestArticles.value = await rssApi.getLatestArticles(limit)
    } catch (error: any) {
      console.error('获取最新文章失败:', error)
    }
  }

  /**
   * 获取分类列表
   */
  async function fetchCategories() {
    try {
      // 响应拦截器已经返回 data.data
      categories.value = await rssApi.getRssCategories()
    } catch (error: any) {
      console.error('获取分类列表失败:', error)
    }
  }

  // ==================== 收藏管理方法 ====================

  /**
   * 获取用户收藏列表
   */
  async function fetchFavorites(page: number = 0, size: number = 20) {
    favoritesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data，这是分页对象
      const result = await rssApi.getUserFavorites(page, size)
      favorites.value = result.content
      favoritesTotal.value = result.totalElements
    } catch (error: any) {
      ElMessage.error(error.message || '获取收藏列表失败')
    } finally {
      favoritesLoading.value = false
    }
  }

  /**
   * 获取用户所有收藏
   */
  async function fetchAllFavorites() {
    try {
      // 响应拦截器已经返回 data.data
      const result = await rssApi.getAllUserFavorites()
      favorites.value = result
      favoritesTotal.value = result.length
    } catch (error: any) {
      ElMessage.error(error.message || '获取收藏列表失败')
    }
  }

  /**
   * 根据标签筛选收藏
   */
  async function fetchFavoritesByTag(tag: string, page: number = 0, size: number = 20) {
    favoritesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data，这是分页对象
      const result = await rssApi.getFavoritesByTag(tag, page, size)
      favorites.value = result.content
      favoritesTotal.value = result.totalElements
    } catch (error: any) {
      ElMessage.error(error.message || '获取收藏失败')
    } finally {
      favoritesLoading.value = false
    }
  }

  /**
   * 获取未读收藏
   */
  async function fetchUnreadFavorites(page: number = 0, size: number = 20) {
    favoritesLoading.value = true
    try {
      // 响应拦截器已经返回 data.data，这是分页对象
      const result = await rssApi.getUnreadFavorites(page, size)
      favorites.value = result.content
      favoritesTotal.value = result.totalElements
    } catch (error: any) {
      ElMessage.error(error.message || '获取未读收藏失败')
    } finally {
      favoritesLoading.value = false
    }
  }

  /**
   * 检查是否收藏了文章
   */
  async function checkArticleFavorite(articleId: number): Promise<boolean> {
    try {
      // 响应拦截器已经返回 data.data
      return await rssApi.checkFavorite(articleId)
    } catch (error: any) {
      console.error('检查收藏状态失败:', error)
      return false
    }
  }

  /**
   * 收藏文章
   */
  async function addFavorite(articleId: number, notes?: string, tags?: string) {
    try {
      // 响应拦截器已经返回 data.data
      const result = await rssApi.addFavorite(articleId, notes, tags)
      ElMessage.success('收藏成功')
      await fetchFavorites()
      await fetchUserStats()
      // 更新当前文章的收藏状态
      if (currentArticle.value && currentArticle.value.id === articleId) {
        currentArticle.value.isFavorite = true
        currentArticle.value.favoriteNotes = notes
        currentArticle.value.favoriteTags = tags
      }
      return result
    } catch (error: any) {
      ElMessage.error(error.message || '收藏失败')
      throw error
    }
  }

  /**
   * 取消收藏
   */
  async function removeFavorite(articleId: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.removeFavorite(articleId)
      ElMessage.success('取消收藏成功')
      await fetchFavorites()
      await fetchUserStats()
      // 更新当前文章的收藏状态
      if (currentArticle.value && currentArticle.value.id === articleId) {
        currentArticle.value.isFavorite = false
        currentArticle.value.favoriteNotes = undefined
        currentArticle.value.favoriteTags = undefined
      }
    } catch (error: any) {
      ElMessage.error(error.message || '取消收藏失败')
      throw error
    }
  }

  /**
   * 更新收藏笔记
   */
  async function updateFavoriteNotes(articleId: number, notes: string) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.updateFavoriteNotes(articleId, notes)
      ElMessage.success('笔记更新成功')
      // 更新本地收藏列表
      const favorite = favorites.value.find(f => f.articleId === articleId)
      if (favorite) {
        favorite.notes = notes
      }
      // 更新当前文章
      if (currentArticle.value && currentArticle.value.id === articleId) {
        currentArticle.value.favoriteNotes = notes
      }
    } catch (error: any) {
      ElMessage.error(error.message || '更新笔记失败')
      throw error
    }
  }

  /**
   * 更新收藏标签
   */
  async function updateFavoriteTags(articleId: number, tags: string) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.updateFavoriteTags(articleId, tags)
      ElMessage.success('标签更新成功')
      // 更新本地收藏列表
      const favorite = favorites.value.find(f => f.articleId === articleId)
      if (favorite) {
        favorite.tags = tags
      }
      // 更新当前文章
      if (currentArticle.value && currentArticle.value.id === articleId) {
        currentArticle.value.favoriteTags = tags
      }
      await fetchUserTags()
    } catch (error: any) {
      ElMessage.error(error.message || '更新标签失败')
      throw error
    }
  }

  /**
   * 切换已读状态
   */
  async function toggleArticleReadStatus(articleId: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.toggleReadStatus(articleId)
      // 更新本地收藏列表
      const favorite = favorites.value.find(f => f.articleId === articleId)
      if (favorite) {
        favorite.isRead = !favorite.isRead
      }
      await fetchUserStats()
    } catch (error: any) {
      ElMessage.error(error.message || '更新状态失败')
    }
  }

  /**
   * 标记为已读
   */
  async function markArticleAsRead(articleId: number) {
    try {
      // 响应拦截器已经返回 data.data
      await rssApi.markAsRead(articleId)
      // 更新本地收藏列表
      const favorite = favorites.value.find(f => f.articleId === articleId)
      if (favorite) {
        favorite.isRead = true
      }
      await fetchUserStats()
    } catch (error: any) {
      ElMessage.error(error.message || '标记失败')
    }
  }

  /**
   * 获取用户标签
   */
  async function fetchUserTags() {
    userTagsLoading.value = true
    try {
      // 响应拦截器已经返回 data.data
      userTags.value = await rssApi.getUserTags()
    } catch (error: any) {
      console.error('获取标签失败:', error)
    } finally {
      userTagsLoading.value = false
    }
  }

  /**
   * 获取用户收藏统计
   */
  async function fetchUserStats() {
    try {
      // 响应拦截器已经返回 data.data
      userStats.value = await rssApi.getUserStats()
    } catch (error: any) {
      console.error('获取收藏统计失败:', error)
    }
  }

  // ==================== 初始化 ====================

  /**
   * 初始化RSS数据
   */
  async function initialize() {
    await Promise.all([
      fetchActiveFeeds(),
      fetchSubscriptions(),
      fetchCategories(),
      fetchPopularArticles(),
      fetchLatestArticles(),
    ])
  }

  return {
    // 状态
    feeds,
    feedsLoading,
    subscriptions,
    subscriptionsLoading,
    favoriteFeeds,
    favoriteFeedsLoading,
    articles,
    articlesLoading,
    articlesTotal,
    currentArticle,
    currentArticleLoading,
    popularArticles,
    latestArticles,
    categories,
    searchResults,
    searchTotal,
    favorites,
    favoritesLoading,
    favoritesTotal,
    userTags,
    userTagsLoading,
    userStats,

    // 计算属性
    subscriptionCount,
    favoriteCount,
    feedsByCategory,
    subscribedFeedIds,
    isSubscribed,

    // 方法
    fetchActiveFeeds,
    fetchFeedsByCategory,
    searchFeeds,
    addFeed,
    deleteFeed,
    fetchFeed,
    fetchSubscriptions,
    subscribe,
    unsubscribe,
    toggleFavorite,
    fetchFavoriteFeeds,
    fetchUserArticles,
    fetchArticlesByFeed,
    fetchArticleDetail,
    searchArticles,
    fetchPopularArticles,
    fetchLatestArticles,
    fetchCategories,
    fetchFavorites,
    fetchAllFavorites,
    fetchFavoritesByTag,
    fetchUnreadFavorites,
    checkArticleFavorite,
    addFavorite,
    removeFavorite,
    updateFavoriteNotes,
    updateFavoriteTags,
    toggleArticleReadStatus,
    markArticleAsRead,
    fetchUserTags,
    fetchUserStats,
    initialize,
  }
})

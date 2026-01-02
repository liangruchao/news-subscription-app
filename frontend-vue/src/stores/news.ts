import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { newsApi } from '@/api'
import type { NewsItem, Subscription } from '@/api'

// 新闻类别
export const NEWS_CATEGORIES = [
  'business',
  'entertainment',
  'general',
  'health',
  'science',
  'sports',
  'technology',
] as const

export type NewsCategory = (typeof NEWS_CATEGORIES)[number]

export const useNewsStore = defineStore('news', () => {
  // State
  const news = ref<NewsItem[]>([])
  const subscriptions = ref<Subscription[]>([])
  const loading = ref(false)

  // Actions
  const fetchUserNews = async () => {
    loading.value = true
    try {
      news.value = await newsApi.getUserNewsApi()
    } finally {
      loading.value = false
    }
  }

  const fetchNewsByCategory = async (category: string) => {
    loading.value = true
    try {
      const data = await newsApi.getNewsByCategoryApi(category)
      return data
    } finally {
      loading.value = false
    }
  }

  const fetchSubscriptions = async () => {
    try {
      subscriptions.value = await newsApi.getSubscriptionsApi()
    } catch (error) {
      subscriptions.value = []
    }
  }

  const addSubscription = async (category: string) => {
    await newsApi.addSubscriptionApi(category)
    await fetchSubscriptions()
  }

  const removeSubscription = async (category: string) => {
    await newsApi.removeSubscriptionApi(category)
    await fetchSubscriptions()
  }

  const hasSubscription = (category: string) => {
    return subscriptions.value.some((sub) => sub.category === category)
  }

  // Getters
  const subscribedCategories = computed(() =>
    subscriptions.value.map((sub) => sub.category)
  )

  const subscriptionCount = computed(() => subscriptions.value.length)

  return {
    // State
    news,
    subscriptions,
    loading,
    // Getters
    subscribedCategories,
    subscriptionCount,
    // Actions
    fetchUserNews,
    fetchNewsByCategory,
    fetchSubscriptions,
    addSubscription,
    removeSubscription,
    hasSubscription,
  }
})

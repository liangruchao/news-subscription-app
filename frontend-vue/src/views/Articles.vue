<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRssStore } from '@/stores/rss'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Article } from '@/api/types'

const router = useRouter()
const rssStore = useRssStore()
const { t } = useI18n()

const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(20)
const selectedFeed = ref<number | null>(null)
const selectedCategory = ref<string | null>(null)

// 计算属性
const hasMore = computed(() => rssStore.articles.length < rssStore.articlesTotal)
const displayedArticles = computed(() => rssStore.articles)

onMounted(async () => {
  await loadArticles()
})

// 加载文章
const loadArticles = async () => {
  loading.value = true
  try {
    if (selectedFeed.value) {
      await rssStore.fetchArticlesByFeed(selectedFeed.value, currentPage.value)
    } else {
      await rssStore.fetchUserArticles(pageSize.value)
    }
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = async () => {
  if (hasMore.value && !loading.value) {
    currentPage.value++
    await loadArticles()
  }
}

// 刷新
const handleRefresh = async () => {
  currentPage.value = 0
  await loadArticles()
}

// 打开文章详情
const openArticle = (article: Article) => {
  // 跳转到文章详情页
  router.push({ name: 'ArticleDetail', params: { id: article.id } })
}

// 格式化时间
const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) {
    const minutes = Math.floor(diff / (1000 * 60))
    return minutes < 1 ? t('articles.justNow') : `${minutes} ${t('articles.minutesAgo')}`
  } else if (hours < 24) {
    return `${hours} ${t('articles.hoursAgo')}`
  } else {
    const days = Math.floor(hours / 24)
    return `${days} ${t('articles.daysAgo')}`
  }
}

// 获取分类名称
const getCategoryName = (category: string): string => {
  return t(`categories.${category}` as any) || category
}
</script>

<template>
  <div class="articles-page">
    <!-- 页面头部 -->
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ t('articles.title') }}</h2>
        <el-button :icon="'Refresh'" circle @click="handleRefresh" :loading="loading" />
      </div>
    </el-card>

    <!-- 文章列表 -->
    <el-card class="articles-card">
      <div v-if="displayedArticles.length === 0 && !loading" class="empty-state">
        <p>{{ t('articles.noArticles') }}</p>
        <el-button type="primary" @click="handleRefresh">
          {{ t('articles.refresh') }}
        </el-button>
      </div>

      <div v-else class="articles-list">
        <div
          v-for="article in displayedArticles"
          :key="article.id"
          class="article-item"
          @click="openArticle(article)"
        >
          <!-- 图片 -->
          <div v-if="article.imageUrl" class="article-image">
            <img :src="article.imageUrl" :alt="article.title" loading="lazy" />
          </div>

          <!-- 内容 -->
          <div class="article-content">
            <h3 class="article-title">{{ article.title }}</h3>

            <p class="article-description">
              {{ article.description || t('articles.noDescription') }}
            </p>

            <div class="article-meta">
              <span class="article-source">
                {{ article.feedTitle || t('articles.unknownSource') }}
              </span>
              <span class="article-category">
                <el-tag size="small">{{ getCategoryName(article.category || 'general') }}</el-tag>
              </span>
              <span class="article-time">{{ formatDate(article.pubDate || article.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button
          @click="loadMore"
          :loading="loading"
          :disabled="loading"
        >
          {{ t('articles.loadMore') }}
        </el-button>
      </div>

      <div v-else-if="displayedArticles.length > 0" class="no-more">
        <p>{{ t('articles.noMore') }}</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.articles-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card {
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  h2 {
    margin: 0;
  }
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    border-color: #409eff;
  }

  .article-image {
    flex-shrink: 0;
    width: 120px;
    height: 80px;
    border-radius: 4px;
    overflow: hidden;
    background: #f5f5f5;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .article-content {
    flex: 1;
    min-width: 0;
  }

  .article-title {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .article-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .article-meta {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #999;
    align-items: center;
  }
}

.load-more,
.no-more {
  text-align: center;
  padding: 20px 0;
}

.no-more {
  color: #999;
}
</style>

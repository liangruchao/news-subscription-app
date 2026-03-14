<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useRssStore } from '@/stores/rss'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Article } from '@/api/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const rssStore = useRssStore()
const { t } = useI18n()

const searchKeyword = ref('')
const currentPage = ref(0)
const pageSize = ref(20)
const searching = ref(false)

const searchResults = ref<Article[]>([])
const totalResults = ref(0)

onMounted(() => {
  const keyword = route.query.q as string
  if (keyword) {
    searchKeyword.value = keyword
    performSearch()
  }
})

// 监听路由变化
watch(() => route.query.q, (newQuery) => {
  if (newQuery && newQuery !== searchKeyword.value) {
    searchKeyword.value = newQuery as string
    performSearch()
  }
})

const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  searching.value = true
  currentPage.value = 0
  try {
    await rssStore.searchArticles(searchKeyword.value, currentPage.value)
    searchResults.value = rssStore.searchResults
    totalResults.value = rssStore.searchTotal
  } catch (error: any) {
    ElMessage.error(error.message || '搜索失败')
  } finally {
    searching.value = false
  }
}

const loadMore = async () => {
  if (searching.value) return

  const hasMore = searchResults.value.length < totalResults.value
  if (!hasMore) {
    ElMessage.info('没有更多结果了')
    return
  }

  searching.value = true
  currentPage.value++
  try {
    await rssStore.searchArticles(searchKeyword.value, currentPage.value)
    // 追加结果
    searchResults.value.push(...rssStore.searchResults)
  } catch (error: any) {
    ElMessage.error(error.message || '搜索失败')
    currentPage.value-- // 回退页码
  } finally {
    searching.value = false
  }
}

const handleSearch = () => {
  // 更新路由
  router.push({ path: '/search', query: { q: searchKeyword.value } })
}

const openArticle = (article: Article) => {
  router.push({ name: 'ArticleDetail', params: { id: article.id } })
}

const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) {
    const minutes = Math.floor(diff / (1000 * 60))
    return minutes < 1 ? '刚刚' : `${minutes} 分钟前`
  } else if (hours < 24) {
    return `${hours} 小时前`
  } else {
    const days = Math.floor(hours / 24)
    return `${days} 天前`
  }
}

const getCategoryName = (category: string): string => {
  return t(`categories.${category}` as any) || category
}

const highlightKeyword = (text: string, keyword: string): string => {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}
</script>

<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <el-card class="search-header">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题、内容..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="'Search'" @click="handleSearch" :loading="searching">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <div v-if="totalResults > 0" class="search-info">
        找到 <strong>{{ totalResults }}</strong> 篇相关文章
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card class="results-card">
      <!-- 加载状态 -->
      <div v-if="searching && searchResults.length === 0" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 无结果 -->
      <div v-else-if="searchResults.length === 0 && !searching" class="empty-state">
        <el-empty :description="searchKeyword ? '未找到相关文章' : '请输入关键词搜索'">
          <el-button v-if="searchKeyword" type="primary" @click="handleSearch">
            重新搜索
          </el-button>
        </el-empty>
      </div>

      <!-- 结果列表 -->
      <div v-else class="results-list">
        <div
          v-for="article in searchResults"
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
            <h3 class="article-title" v-html="highlightKeyword(article.title, searchKeyword)" />

            <p class="article-description">
              {{ article.description || '暂无描述' }}
            </p>

            <div class="article-meta">
              <span class="article-source">
                {{ article.feedTitle || '未知来源' }}
              </span>
              <span class="article-category">
                <el-tag size="small">{{ getCategoryName(article.category || 'general') }}</el-tag>
              </span>
              <span class="article-time">{{ formatDate(article.pubDate || article.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="searchResults.length < totalResults" class="load-more">
          <el-button
            @click="loadMore"
            :loading="searching"
            :disabled="searching"
          >
            加载更多
          </el-button>
        </div>

        <div v-else-if="searchResults.length > 0" class="no-more">
          <p>没有更多结果了</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.search-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-header {
  .search-box {
    margin-bottom: 16px;
  }

  .search-info {
    font-size: 14px;
    color: #666;
    padding: 8px 0;

    strong {
      color: #409eff;
      font-size: 18px;
    }
  }
}

.loading-state {
  padding: 40px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.results-list {
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

    :deep(mark) {
      background-color: #fff3cd;
      padding: 0 2px;
      border-radius: 2px;
    }
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

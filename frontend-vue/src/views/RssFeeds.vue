<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRssStore } from '@/stores/rss'
import { useI18n } from 'vue-i18n'
import type { RssFeed } from '@/api/types'

const rssStore = useRssStore()
const { t } = useI18n()

const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('all')
const showAddDialog = ref(false)
const newFeedUrl = ref('')

// 计算属性
const categories = computed(() => ['all', ...rssStore.categories])
const filteredFeeds = computed(() => {
  let feeds = rssStore.feeds

  if (selectedCategory.value !== 'all') {
    feeds = feeds.filter(f => f.category === selectedCategory.value)
  }

  if (searchKeyword.value) {
    feeds = feeds.filter(f =>
      f.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      f.description?.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }

  return feeds
})

onMounted(async () => {
  loading.value = true
  await Promise.all([
    rssStore.fetchActiveFeeds(),
    rssStore.fetchCategories(),
    rssStore.fetchSubscriptions()  // 获取用户订阅列表
  ])
  loading.value = false
})

// 添加RSS源
const handleAddFeed = async () => {
  if (!newFeedUrl.value) {
    return
  }

  try {
    await rssStore.addFeed({ url: newFeedUrl.value })
    newFeedUrl.value = ''
    showAddDialog.value = false
  } catch (error) {
    // Error already handled in store
  }
}

// 订阅/取消订阅
const handleSubscribe = async (feed: RssFeed) => {
  if (rssStore.isSubscribed(feed.id)) {
    await rssStore.unsubscribe(feed.id)
  } else {
    await rssStore.subscribe(feed.id)
  }
}

// 抓取RSS源
const handleFetch = async (feedId: number) => {
  await rssStore.fetchFeed(feedId)
}

// 切换收藏
const handleToggleFavorite = async (feedId: number) => {
  await rssStore.toggleFavorite(feedId)
}

// 删除RSS源
const handleDelete = async (feedId: number) => {
  await rssStore.deleteFeed(feedId)
}

// 获取分类名称
const getCategoryName = (category: string): string => {
  if (category === 'all') return t('rss.allCategories')
  return t(`categories.${category}` as any) || category
}
</script>

<template>
  <div class="rss-feeds-page" v-loading="loading">
    <!-- 页面头部 -->
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ t('rss.title') }}</h2>
        <el-button type="primary" @click="showAddDialog = true">
          {{ t('rss.addFeed') }}
        </el-button>
      </div>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('rss.searchPlaceholder')"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="12">
          <el-select v-model="selectedCategory" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat"
              :value="cat"
              :label="getCategoryName(cat)"
            />
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <!-- RSS源列表 -->
    <el-card class="feeds-card">
      <div v-if="filteredFeeds.length === 0" class="empty-state">
        <p>{{ t('rss.noFeeds') }}</p>
      </div>

      <div v-else class="feeds-list">
        <div
          v-for="feed in filteredFeeds"
          :key="feed.id"
          class="feed-item"
        >
          <div class="feed-header">
            <h3>{{ feed.title }}</h3>
            <el-tag
              :type="rssStore.isSubscribed(feed.id) ? 'success' : 'info'"
              size="small"
            >
              {{ rssStore.isSubscribed(feed.id) ? t('rss.subscribed') : t('rss.notSubscribed') }}
            </el-tag>
          </div>

          <p class="feed-description">{{ feed.description || t('rss.noDescription') }}</p>

          <div class="feed-meta">
            <span>{{ t('rss.category') }}: {{ getCategoryName(feed.category || 'general') }}</span>
            <span>{{ t('rss.articles') }}: {{ feed.articleCount }}</span>
          </div>

          <div class="feed-actions">
            <el-button
              v-if="!rssStore.isSubscribed(feed.id)"
              type="primary"
              size="small"
              @click="handleSubscribe(feed)"
            >
              {{ t('rss.subscribe') }}
            </el-button>
            <el-button
              v-else
              type="danger"
              size="small"
              plain
              @click="handleSubscribe(feed)"
            >
              {{ t('rss.unsubscribe') }}
            </el-button>

            <el-button
              size="small"
              @click="handleFetch(feed.id)"
            >
              {{ t('rss.fetch') }}
            </el-button>

            <el-button
              size="small"
              :type="feed.isActive ? 'warning' : 'success'"
              @click="handleToggleFavorite(feed.id)"
            >
              {{ feed.isActive ? t('rss.deactivate') : t('rss.activate') }}
            </el-button>

            <el-button
              size="small"
              type="danger"
              @click="handleDelete(feed.id)"
            >
              {{ t('rss.delete') }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加RSS源对话框 -->
    <el-dialog v-model="showAddDialog" :title="t('rss.addFeed')">
      <el-form @submit.prevent="handleAddFeed">
        <el-form-item :label="t('rss.feedUrl')">
          <el-input
            v-model="newFeedUrl"
            :placeholder="t('rss.feedUrlPlaceholder')"
            type="url"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">
          {{ t('common.cancel') }}
        </el-button>
        <el-button type="primary" @click="handleAddFeed">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.rss-feeds-page {
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

.feeds-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.feed-item {
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .feed-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    h3 {
      margin: 0;
      font-size: 16px;
    }
  }

  .feed-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 12px;
    min-height: 40px;
  }

  .feed-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #999;
    margin-bottom: 12px;
  }

  .feed-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}
</style>

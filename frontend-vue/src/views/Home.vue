<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useNewsStore, NEWS_CATEGORIES } from '@/stores/news'
import { useUiStore } from '@/stores/ui'
import { useI18n } from 'vue-i18n'
import type { NewsCategory } from '@/stores/news'

const newsStore = useNewsStore()
const uiStore = useUiStore()
const { t } = useI18n()

const loading = ref(false)

onMounted(async () => {
  loading.value = true
  await Promise.all([newsStore.fetchSubscriptions(), newsStore.fetchUserNews()])
  loading.value = false
})

const handleSubscribe = async (category: NewsCategory) => {
  if (newsStore.hasSubscription(category)) {
    await newsStore.removeSubscription(category)
    uiStore.showSuccess(t('home.unsubscribeSuccess'))
  } else {
    await newsStore.addSubscription(category)
    uiStore.showSuccess(t('home.subscribeSuccess'))
  }
  await newsStore.fetchUserNews()
}

const handleRefresh = async () => {
  await newsStore.fetchUserNews()
}

const getCategoryName = (category: string): string => {
  return t(`categories.${category}` as any)
}
</script>

<template>
  <div class="home-page" v-loading="loading">
    <!-- 订阅管理 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <h3>{{ t('home.mySubscriptions') }}</h3>
          <el-tag type="info">{{ newsStore.subscriptionCount }} / {{ NEWS_CATEGORIES.length }}</el-tag>
        </div>
      </template>

      <div v-if="newsStore.subscriptionCount === 0" class="empty-state">
        <p>{{ t('home.noSubscriptions') }}</p>
      </div>

      <el-checkbox-group v-else class="category-list">
        <el-checkbox
          v-for="cat in newsStore.subscribedCategories"
          :key="cat"
          :value="cat"
          :checked="true"
          @change="(val: boolean) => !val && handleSubscribe(cat as NewsCategory)"
        >
          {{ getCategoryName(cat) }}
        </el-checkbox>
      </el-checkbox-group>

      <el-divider />

      <div class="available-categories">
        <h4>{{ t('home.addSubscription') }}</h4>
        <div class="category-buttons">
          <el-button
            v-for="cat in NEWS_CATEGORIES"
            :key="cat"
            :type="newsStore.hasSubscription(cat) ? 'default' : 'primary'"
            :plain="newsStore.hasSubscription(cat)"
            size="small"
            @click="() => handleSubscribe(cat)"
          >
            {{ getCategoryName(cat) }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 新闻列表 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <h3>{{ t('home.latestNews') }}</h3>
          <el-button type="primary" :icon="'Refresh'" circle @click="handleRefresh" />
        </div>
      </template>

      <div v-if="newsStore.news.length === 0" class="empty-state">
        <p>{{ t('home.noNews') }}</p>
      </div>

      <div v-else class="news-list">
        <div v-for="item in newsStore.news" :key="item.url" class="news-item">
          <h4 class="news-title">
            <a :href="item.url" target="_blank">{{ item.title }}</a>
          </h4>
          <p class="news-description">{{ item.description }}</p>
          <div class="news-meta">
            <span class="news-source">{{ item.source?.name || 'Unknown' }}</span>
            <span class="news-time">{{ new Date(item.publishedAt).toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 { margin: 0; }
  }
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.available-categories {
  h4 { margin: 0 0 12px 0; }

  .category-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-item {
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .news-title {
    margin: 0 0 8px 0;

    a {
      text-decoration: none;
      color: #333;
      font-size: 16px;

      &:hover {
        color: #409eff;
      }
    }
  }

  .news-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .news-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #999;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>

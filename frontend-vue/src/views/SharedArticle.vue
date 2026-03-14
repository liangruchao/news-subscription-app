<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { rssApi } from '@/api'
import type { ArticleShare } from '@/api/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const shareCode = computed(() => route.params.code as string)

const share = ref<ArticleShare | null>(null)
const loading = ref(true)
const notFound = ref(false)

onMounted(async () => {
  await loadShare()
})

const loadShare = async () => {
  loading.value = true
  try {
    share.value = await rssApi.getShareByCode(shareCode.value)
  } catch (error: any) {
    console.error('加载分享失败:', error)
    notFound.value = true
    ElMessage.error('分享不存在或已过期')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openOriginal = () => {
  if (share.value?.articleLink) {
    window.open(share.value.articleLink, '_blank')
  }
}
</script>

<template>
  <div class="shared-article-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 未找到分享 -->
    <div v-else-if="notFound || !share" class="not-found-state">
      <el-result icon="warning" title="分享不存在" sub-title="该分享链接可能已过期或不存在">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">
            返回首页
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 文章内容 -->
    <div v-else class="shared-article">
      <div class="article-header">
        <h1 class="article-title">{{ share.title || share.articleTitle }}</h1>

        <div class="article-meta">
          <span class="meta-item">
            <el-icon><View /></el-icon>
            浏览 {{ share.viewCount }} 次
          </span>
          <span v-if="share.expiresAt" class="meta-item">
            <el-icon><Clock /></el-icon>
            有效期至 {{ formatDate(share.expiresAt) }}
          </span>
        </div>
      </div>

      <!-- 分享者自定义描述 -->
      <div v-if="share.description" class="share-description">
        <p>{{ share.description }}</p>
      </div>

      <!-- 文章图片 -->
      <div v-if="share.articleImageUrl" class="article-image">
        <img :src="share.articleImageUrl" :alt="share.articleTitle" />
      </div>

      <!-- 文章摘要 -->
      <div v-if="share.articleContent" class="article-content">
        <div v-html="share.articleContent" />
      </div>

      <!-- 文章元信息 -->
      <div v-if="share.articleAuthor || share.articlePubDate" class="article-footer">
        <div v-if="share.articleAuthor" class="footer-item">
          <strong>作者：</strong>{{ share.articleAuthor }}
        </div>
        <div v-if="share.articlePubDate" class="footer-item">
          <strong>发布时间：</strong>{{ formatDate(share.articlePubDate) }}
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" :icon="'Link'" @click="openOriginal">
          阅读原文
        </el-button>
        <el-button @click="$router.push('/')">
          返回首页
        </el-button>
      </div>

      <!-- 分享提示 -->
      <div class="share-tip">
        <el-alert type="info" :closable="false">
          <template #title>
            📰 本文来自新闻订阅系统
          </template>
          <p>喜欢这篇文章？立即注册订阅更多优质内容！</p>
          <el-button type="primary" link @click="$router.push('/register')">
            立即注册
          </el-button>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.shared-article-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

.loading-state {
  padding: 40px;
}

.shared-article {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.article-header {
  margin-bottom: 24px;

  .article-title {
    font-size: 32px;
    font-weight: 700;
    color: #333;
    margin: 0 0 16px 0;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    gap: 16px;
    font-size: 14px;
    color: #666;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.share-description {
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  margin-bottom: 24px;
  font-size: 16px;
  line-height: 1.8;
  color: #666;
}

.article-image {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;

  img {
    width: 100%;
    height: auto;
    display: block;
  }
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;

  :deep(img) {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 16px 0;
  }

  :deep(p) {
    margin-bottom: 16px;
  }

  :deep(h2),
  :deep(h3),
  :deep(h4) {
    margin-top: 24px;
    margin-bottom: 12px;
  }
}

.article-footer {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  gap: 24px;
  flex-wrap: wrap;

  .footer-item {
    font-size: 14px;
    color: #666;

    strong {
      color: #333;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.share-tip {
  margin-top: 40px;

  p {
    margin: 8px 0;
  }
}
</style>

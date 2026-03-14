<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRssStore } from '@/stores/rss'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const rssStore = useRssStore()
const { t } = useI18n()

const articleId = computed(() => Number(route.params.id))
const notes = ref('')
const tags = ref('')
const notesDialogVisible = ref(false)
const tagsDialogVisible = ref(false)
const shareDialogVisible = ref(false)
const shareUrl = ref('')

onMounted(async () => {
  await loadArticleDetail()
  await rssStore.fetchUserTags()
  await rssStore.fetchUserStats()
})

const loadArticleDetail = async () => {
  try {
    await rssStore.fetchArticleDetail(articleId.value)
    const article = rssStore.currentArticle
    if (article) {
      notes.value = article.favoriteNotes || ''
      tags.value = article.favoriteTags || ''
    }
  } catch (error) {
    ElMessage.error(t('articleDetail.loadFailed'))
    router.push('/articles')
  }
}

const toggleFavorite = async () => {
  const article = rssStore.currentArticle
  if (!article) return

  try {
    if (article.isFavorite) {
      await rssStore.removeFavorite(articleId.value)
    } else {
      await rssStore.addFavorite(articleId.value, notes.value, tags.value)
    }
    await loadArticleDetail()
  } catch (error) {
    // Error already handled in store
  }
}

const toggleReadStatus = async () => {
  try {
    await rssStore.toggleArticleReadStatus(articleId.value)
    await loadArticleDetail()
  } catch (error) {
    // Error already handled in store
  }
}

const shareArticle = async () => {
  const article = rssStore.currentArticle
  if (!article) return

  // 生成分享链接（使用当前URL）
  shareUrl.value = window.location.href
  shareDialogVisible.value = true
}

const copyShareUrl = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success(t('articleDetail.copied'))
  } catch {
    ElMessage.error(t('articleDetail.copyFailed'))
  }
}

const saveNotes = async () => {
  try {
    await rssStore.updateFavoriteNotes(articleId.value, notes.value)
    notesDialogVisible.value = false
    await loadArticleDetail()
  } catch (error) {
    // Error already handled in store
  }
}

const saveTags = async () => {
  try {
    await rssStore.updateFavoriteTags(articleId.value, tags.value)
    tagsDialogVisible.value = false
    await loadArticleDetail()
  } catch (error) {
    // Error already handled in store
  }
}

const openOriginal = () => {
  const article = rssStore.currentArticle
  if (article?.link) {
    window.open(article.link, '_blank')
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

const getCategoryName = (category: string): string => {
  return t(`categories.${category}` as any) || category
}
</script>

<template>
  <div class="article-detail-page">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button :icon="'ArrowLeft'" @click="router.push('/articles')">
        {{ t('articleDetail.back') }}
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="rssStore.currentArticleLoading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 文章内容 -->
    <div v-else-if="rssStore.currentArticle" class="article-detail">
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button
          :type="rssStore.currentArticle.isFavorite ? 'primary' : 'default'"
          :icon="rssStore.currentArticle.isFavorite ? 'StarFilled' : 'Star'"
          @click="toggleFavorite"
        >
          {{ rssStore.currentArticle.isFavorite ? t('articleDetail.favorited') : t('articleDetail.favorite') }}
        </el-button>

        <el-button
          v-if="rssStore.currentArticle.isFavorite"
          :type="rssStore.currentArticle.isRead ? 'success' : 'info'"
          :icon="rssStore.currentArticle.isRead ? 'CircleCheckFilled' : 'CircleCheck'"
          @click="toggleReadStatus"
        >
          {{ rssStore.currentArticle.isRead ? t('articleDetail.read') : t('articleDetail.unread') }}
        </el-button>

        <el-button
          :icon="'Share'"
          @click="shareArticle"
        >
          {{ t('articleDetail.share') }}
        </el-button>

        <el-button
          v-if="rssStore.currentArticle.isFavorite"
          :icon="'Edit'"
          @click="notesDialogVisible = true"
        >
          {{ t('articleDetail.notes') }}
        </el-button>

        <el-button
          v-if="rssStore.currentArticle.isFavorite"
          :icon="'PriceTag'"
          @click="tagsDialogVisible = true"
        >
          {{ t('articleDetail.tags') }}
        </el-button>

        <el-button :icon="'Link'" @click="openOriginal">
          {{ t('articleDetail.openOriginal') }}
        </el-button>
      </div>

      <!-- 文章头部 -->
      <div class="article-header">
        <h1 class="article-title">{{ rssStore.currentArticle.title }}</h1>

        <div class="article-meta">
          <span class="meta-item">
            <el-icon><Source /></el-icon>
            {{ rssStore.currentArticle.feedTitle || t('articleDetail.unknownSource') }}
          </span>

          <span v-if="rssStore.currentArticle.category" class="meta-item">
            <el-tag size="small">{{ getCategoryName(rssStore.currentArticle.category) }}</el-tag>
          </span>

          <span v-if="rssStore.currentArticle.author" class="meta-item">
            <el-icon><User /></el-icon>
            {{ rssStore.currentArticle.author }}
          </span>

          <span v-if="rssStore.currentArticle.pubDate" class="meta-item">
            <el-icon><Clock /></el-icon>
            {{ formatDate(rssStore.currentArticle.pubDate) }}
          </span>

          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ rssStore.currentArticle.viewCount || 0 }}
          </span>
        </div>
      </div>

      <!-- 文章图片 -->
      <div v-if="rssStore.currentArticle.imageUrl" class="article-image">
        <img :src="rssStore.currentArticle.imageUrl" :alt="rssStore.currentArticle.title" />
      </div>

      <!-- 文章摘要 -->
      <div v-if="rssStore.currentArticle.description" class="article-description">
        <p>{{ rssStore.currentArticle.description }}</p>
      </div>

      <!-- 文章正文 -->
      <div class="article-content">
        <div v-html="rssStore.currentArticle.content || rssStore.currentArticle.description" />
      </div>

      <!-- 收藏笔记显示 -->
      <div v-if="rssStore.currentArticle.isFavorite && notes" class="favorite-notes">
        <h3>{{ t('articleDetail.myNotes') }}</h3>
        <p>{{ notes }}</p>
      </div>

      <!-- 收藏标签显示 -->
      <div v-if="rssStore.currentArticle.isFavorite && tags" class="favorite-tags">
        <h3>{{ t('articleDetail.myTags') }}</h3>
        <div class="tags-list">
          <el-tag
            v-for="(tag, index) in tags.split(',').filter(t => t.trim())"
            :key="index"
            size="small"
          >
            {{ tag.trim() }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 未找到文章 -->
    <div v-else class="empty-state">
      <el-empty :description="t('articleDetail.notFound')" />
    </div>

    <!-- 笔记编辑对话框 -->
    <el-dialog v-model="notesDialogVisible" :title="t('articleDetail.editNotes')" width="600px">
      <el-input
        v-model="notes"
        type="textarea"
        :rows="8"
        :placeholder="t('articleDetail.notesPlaceholder')"
      />
      <template #footer>
        <el-button @click="notesDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveNotes">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 标签编辑对话框 -->
    <el-dialog v-model="tagsDialogVisible" :title="t('articleDetail.editTags')" width="600px">
      <el-input
        v-model="tags"
        :placeholder="t('articleDetail.tagsPlaceholder')"
      />
      <div class="tags-hint">
        <el-text type="info" size="small">
          {{ t('articleDetail.tagsHint') }}
        </el-text>
      </div>
      <div v-if="rssStore.userTags.length > 0" class="existing-tags">
        <el-text type="info" size="small">{{ t('articleDetail.existingTags') }}</el-text>
        <div class="tags-list">
          <el-tag
            v-for="tag in rssStore.userTags"
            :key="tag"
            size="small"
            style="margin: 4px; cursor: pointer;"
            @click="tags = tags ? tags + ',' + tag : tag"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="tagsDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveTags">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" :title="t('articleDetail.shareArticle')" width="500px">
      <el-input v-model="shareUrl" readonly>
        <template #append>
          <el-button :icon="'CopyDocument'" @click="copyShareUrl">
            {{ t('articleDetail.copy') }}
          </el-button>
        </template>
      </el-input>
      <div class="share-hint">
        <el-text type="info" size="small">
          {{ t('articleDetail.shareHint') }}
        </el-text>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.article-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.loading-state {
  padding: 40px;
}

.article-detail {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.article-header {
  margin-bottom: 24px;

  .article-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin: 0 0 16px 0;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    font-size: 14px;
    color: #666;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
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

.article-description {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 16px;
  line-height: 1.8;
  color: #666;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;

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

  :deep(pre) {
    background: #f5f5f5;
    padding: 16px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 16px 0;
  }

  :deep(blockquote) {
    border-left: 4px solid #409eff;
    padding-left: 16px;
    margin: 16px 0;
    color: #666;
  }
}

.favorite-notes {
  margin-top: 32px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #409eff;
  }

  p {
    margin: 0;
    white-space: pre-wrap;
    line-height: 1.6;
  }
}

.favorite-tags {
  margin-top: 24px;

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #666;
  }

  .tags-list {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.tags-hint {
  margin-top: 12px;
}

.existing-tags {
  margin-top: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;

  .tags-list {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
  }
}

.share-hint {
  margin-top: 12px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>

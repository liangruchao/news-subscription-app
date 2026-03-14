<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRssStore } from '@/stores/rss'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UserFavorite } from '@/api/types'

const router = useRouter()
const rssStore = useRssStore()
const { t } = useI18n()

const currentPage = ref(0)
const pageSize = ref(20)
const selectedTag = ref<string>('')
const filterStatus = ref<string>('all') // all, unread, read
const notesDialogVisible = ref(false)
const tagsDialogVisible = ref(false)
const currentFavorite = ref<UserFavorite | null>(null)
const editNotes = ref('')
const editTags = ref('')

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  await Promise.all([
    rssStore.fetchFavorites(currentPage.value, pageSize.value),
    rssStore.fetchUserTags(),
    rssStore.fetchUserStats()
  ])
}

const handleFilterByTag = async (tag: string) => {
  selectedTag.value = tag
  currentPage.value = 0
  if (tag) {
    await rssStore.fetchFavoritesByTag(tag, currentPage.value, pageSize.value)
  } else {
    await rssStore.fetchFavorites(currentPage.value, pageSize.value)
  }
}

const handleFilterByStatus = async (status: string) => {
  filterStatus.value = status
  currentPage.value = 0
  if (status === 'unread') {
    await rssStore.fetchUnreadFavorites(currentPage.value, pageSize.value)
  } else if (status === 'read') {
    // 获取已读收藏（通过计算属性过滤）
    await rssStore.fetchFavorites(currentPage.value, pageSize.value)
  } else {
    await rssStore.fetchFavorites(currentPage.value, pageSize.value)
  }
}

const handlePageChange = async (page: number) => {
  currentPage.value = page - 1
  await loadData()
}

const openArticle = (articleId: number) => {
  router.push(`/articles/${articleId}`)
}

const openOriginal = (link: string) => {
  window.open(link, '_blank')
}

const editFavoriteNotes = (favorite: UserFavorite) => {
  currentFavorite.value = favorite
  editNotes.value = favorite.notes || ''
  notesDialogVisible.value = true
}

const saveNotes = async () => {
  if (!currentFavorite.value) return
  try {
    await rssStore.updateFavoriteNotes(currentFavorite.value.articleId, editNotes.value)
    await loadData()
    notesDialogVisible.value = false
    ElMessage.success(t('favorites.notesUpdated'))
  } catch (error) {
    // Error already handled in store
  }
}

const editFavoriteTags = (favorite: UserFavorite) => {
  currentFavorite.value = favorite
  editTags.value = favorite.tags || ''
  tagsDialogVisible.value = true
}

const saveTags = async () => {
  if (!currentFavorite.value) return
  try {
    await rssStore.updateFavoriteTags(currentFavorite.value.articleId, editTags.value)
    await loadData()
    tagsDialogVisible.value = false
    ElMessage.success(t('favorites.tagsUpdated'))
  } catch (error) {
    // Error already handled in store
  }
}

const toggleReadStatus = async (favorite: UserFavorite) => {
  try {
    await rssStore.toggleArticleReadStatus(favorite.articleId)
    await loadData()
  } catch (error) {
    // Error already handled in store
  }
}

const removeFavorite = async (favorite: UserFavorite) => {
  try {
    await ElMessageBox.confirm(
      t('favorites.removeConfirm'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
    )
    await rssStore.removeFavorite(favorite.articleId)
    await loadData()
    ElMessage.success(t('favorites.removed'))
  } catch (error) {
    // User cancelled or error handled in store
  }
}

const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return t('favorites.today')
  } else if (days < 7) {
    return `${days} ${t('favorites.daysAgo')}`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 过滤后的收藏列表（用于处理已读/未读筛选）
const filteredFavorites = computed(() => {
  let list = rssStore.favorites
  if (filterStatus.value === 'unread') {
    return list.filter(f => !f.isRead)
  } else if (filterStatus.value === 'read') {
    return list.filter(f => f.isRead)
  }
  return list
})

// 分页后的收藏列表
const displayedFavorites = computed(() => {
  return filteredFavorites.value
})

const hasMore = computed(() => {
  return (currentPage.value + 1) * pageSize.value < rssStore.favoritesTotal
})
</script>

<template>
  <div class="favorites-page">
    <!-- 页面头部 -->
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ t('favorites.title') }}</h2>
        <el-button :icon="'Refresh'" @click="loadData" :loading="rssStore.favoritesLoading">
          {{ t('favorites.refresh') }}
        </el-button>
      </div>

      <!-- 统计信息 -->
      <div v-if="rssStore.userStats" class="stats-bar">
        <div class="stat-item">
          <span class="stat-label">{{ t('favorites.total') }}:</span>
          <span class="stat-value">{{ rssStore.userStats.totalCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">{{ t('favorites.unread') }}:</span>
          <span class="stat-value unread-count">{{ rssStore.userStats.unreadCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">{{ t('favorites.read') }}:</span>
          <span class="stat-value">{{ rssStore.userStats.readCount }}</span>
        </div>
      </div>
    </el-card>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <div class="filter-content">
        <!-- 状态筛选 -->
        <div class="filter-group">
          <span class="filter-label">{{ t('favorites.filterByStatus') }}:</span>
          <el-radio-group v-model="filterStatus" @change="handleFilterByStatus">
            <el-radio-button value="all">{{ t('favorites.all') }}</el-radio-button>
            <el-radio-button value="unread">{{ t('favorites.unread') }}</el-radio-button>
            <el-radio-button value="read">{{ t('favorites.read') }}</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 标签筛选 -->
        <div v-if="rssStore.userTags.length > 0" class="filter-group">
          <span class="filter-label">{{ t('favorites.filterByTag') }}:</span>
          <div class="tags-filter">
            <el-tag
              :type="selectedTag === '' ? 'primary' : 'info'"
              style="margin: 4px; cursor: pointer;"
              @click="handleFilterByTag('')"
            >
              {{ t('favorites.all') }}
            </el-tag>
            <el-tag
              v-for="tag in rssStore.userTags"
              :key="tag"
              :type="selectedTag === tag ? 'primary' : 'info'"
              style="margin: 4px; cursor: pointer;"
              @click="handleFilterByTag(tag)"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 收藏列表 -->
    <el-card class="favorites-card">
      <div v-if="displayedFavorites.length === 0 && !rssStore.favoritesLoading" class="empty-state">
        <el-empty :description="t('favorites.noFavorites')" />
      </div>

      <div v-else class="favorites-list">
        <div
          v-for="favorite in displayedFavorites"
          :key="favorite.id"
          class="favorite-item"
          :class="{ unread: !favorite.isRead }"
        >
          <!-- 已读标记 -->
          <div class="read-status">
            <el-icon
              :class="{ 'is-read': favorite.isRead }"
              @click="toggleReadStatus(favorite)"
              style="cursor: pointer;"
            >
              <CircleCheck v-if="favorite.isRead" />
              <CircleClose v-else />
            </el-icon>
          </div>

          <!-- 图片 -->
          <div v-if="favorite.articleImageUrl" class="favorite-image">
            <img :src="favorite.articleImageUrl" :alt="favorite.articleTitle" loading="lazy" />
          </div>

          <!-- 内容 -->
          <div class="favorite-content">
            <h3 class="favorite-title" @click="openArticle(favorite.articleId)">
              {{ favorite.articleTitle }}
            </h3>

            <p v-if="favorite.articleDescription" class="favorite-description">
              {{ favorite.articleDescription }}
            </p>

            <!-- 笔记 -->
            <div v-if="favorite.notes" class="favorite-notes">
              <el-icon><EditPen /></el-icon>
              <span>{{ favorite.notes }}</span>
            </div>

            <!-- 标签 -->
            <div v-if="favorite.tags" class="favorite-tags">
              <el-icon><PriceTag /></el-icon>
              <el-tag
                v-for="(tag, index) in favorite.tags.split(',').filter(t => t.trim())"
                :key="index"
                size="small"
              >
                {{ tag.trim() }}
              </el-tag>
            </div>

            <!-- 元信息 -->
            <div class="favorite-meta">
              <span class="meta-time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(favorite.createdAt) }}
              </span>
              <span v-if="favorite.articlePubDate" class="meta-pub-date">
                {{ formatDate(favorite.articlePubDate) }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="favorite-actions">
            <el-button-group>
              <el-button :icon="'View'" size="small" @click="openArticle(favorite.articleId)">
                {{ t('favorites.view') }}
              </el-button>
              <el-button :icon="'Link'" size="small" @click="openOriginal(favorite.articleLink)">
                {{ t('favorites.original') }}
              </el-button>
            </el-button-group>

            <el-dropdown style="margin-left: 8px;">
              <el-button :icon="'More'" size="small" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :icon="'Edit'" @click="editFavoriteNotes(favorite)">
                    {{ t('favorites.editNotes') }}
                  </el-dropdown-item>
                  <el-dropdown-item :icon="'PriceTag'" @click="editFavoriteTags(favorite)">
                    {{ t('favorites.editTags') }}
                  </el-dropdown-item>
                  <el-dropdown-item :icon="'Delete'" @click="removeFavorite(favorite)">
                    {{ t('favorites.remove') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="rssStore.favoritesTotal > pageSize" class="pagination">
        <el-pagination
          :current-page="currentPage + 1"
          :page-size="pageSize"
          :total="rssStore.favoritesTotal"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 笔记编辑对话框 -->
    <el-dialog v-model="notesDialogVisible" :title="t('favorites.editNotes')" width="600px">
      <el-input
        v-model="editNotes"
        type="textarea"
        :rows="8"
        :placeholder="t('favorites.notesPlaceholder')"
      />
      <template #footer>
        <el-button @click="notesDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveNotes">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 标签编辑对话框 -->
    <el-dialog v-model="tagsDialogVisible" :title="t('favorites.editTags')" width="600px">
      <el-input
        v-model="editTags"
        :placeholder="t('favorites.tagsPlaceholder')"
      />
      <div class="tags-hint">
        <el-text type="info" size="small">
          {{ t('favorites.tagsHint') }}
        </el-text>
      </div>
      <div v-if="rssStore.userTags.length > 0" class="existing-tags">
        <el-text type="info" size="small">{{ t('favorites.existingTags') }}</el-text>
        <div class="tags-list">
          <el-tag
            v-for="tag in rssStore.userTags"
            :key="tag"
            size="small"
            style="margin: 4px; cursor: pointer;"
            @click="editTags = editTags ? editTags + ',' + tag : tag"
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
  </div>
</template>

<style scoped lang="scss">
.favorites-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card {
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  h2 {
    margin: 0;
  }

  .stats-bar {
    display: flex;
    gap: 24px;
    padding-top: 16px;
    border-top: 1px solid #eee;

    .stat-item {
      display: flex;
      gap: 8px;

      .stat-label {
        color: #666;
      }

      .stat-value {
        font-weight: 600;
        color: #333;

        &.unread-count {
          color: #409eff;
        }
      }
    }
  }
}

.filter-card {
  .filter-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .filter-group {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;

    .filter-label {
      font-weight: 500;
      color: #666;
    }

    .tags-filter {
      display: flex;
      flex-wrap: wrap;
    }
  }
}

.empty-state {
  padding: 60px 0;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.favorite-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  &.unread {
    background: #f9f9f9;
    border-left: 3px solid #409eff;
  }

  .read-status {
    display: flex;
    align-items: flex-start;
    padding-top: 4px;

    .el-icon {
      font-size: 20px;
      color: #c0c4cc;

      &.is-read {
        color: #67c23a;
      }
    }
  }

  .favorite-image {
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

  .favorite-content {
    flex: 1;
    min-width: 0;
  }

  .favorite-title {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      color: #409eff;
    }
  }

  .favorite-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .favorite-notes {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    padding: 8px;
    background: #f0f9ff;
    border-radius: 4px;
    margin-bottom: 8px;
    font-size: 13px;
    color: #409eff;

    .el-icon {
      flex-shrink: 0;
      margin-top: 2px;
    }

    span {
      white-space: pre-wrap;
      word-break: break-word;
    }
  }

  .favorite-tags {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    flex-wrap: wrap;

    .el-icon {
      color: #909399;
    }
  }

  .favorite-meta {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #999;

    .meta-time,
    .meta-pub-date {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .favorite-actions {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 8px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
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
</style>

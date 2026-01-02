<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { messageApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { useI18n } from 'vue-i18n'
import { ElMessageBox } from 'element-plus'
import type { Message } from '@/api'

const { t } = useI18n()
const uiStore = useUiStore()

const messages = ref<Message[]>([])
const loading = ref(false)
const refreshLoading = ref(false)

// 未读消息数量
const unreadCount = computed(() => {
  return messages.value.filter((m) => !m.isRead).length
})

// 消息类型对应的标签类型
const getTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    system: 'danger',
    subscription: 'warning',
    profile: 'info',
    announcement: 'success',
  }
  return typeMap[type] || 'info'
}

// 消息类型对应的翻译
const getTypeText = (type: string) => {
  const typeKey = `messages.${type}` as const
  return t(typeKey)
}

// 格式化时间
const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN')
}

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    messages.value = await messageApi.getMessagesApi()
  } catch (error: any) {
    uiStore.showError(error.message || '获取消息失败')
  } finally {
    loading.value = false
  }
}

// 刷新消息列表
const handleRefresh = async () => {
  refreshLoading.value = true
  try {
    messages.value = await messageApi.getMessagesApi()
    uiStore.showSuccess('刷新成功')
  } catch (error: any) {
    uiStore.showError(error.message || '刷新失败')
  } finally {
    refreshLoading.value = false
  }
}

// 标记单个消息为已读/未读
const handleToggleRead = async (message: Message) => {
  const successMsg = message.isRead ? 'messages.markedAsUnread' : 'messages.markedAsRead'

  try {
    await messageApi.markAsReadApi(message.id)
    message.isRead = !message.isRead
    uiStore.showSuccess(t(successMsg))
  } catch (error: any) {
    uiStore.showError(error.message || '操作失败')
  }
}

// 删除消息
const handleDelete = async (message: Message) => {
  try {
    await ElMessageBox.confirm(
      t('messages.deleteConfirm'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
    )

    await messageApi.deleteMessageApi(message.id)
    messages.value = messages.value.filter((m) => m.id !== message.id)
    uiStore.showSuccess(t('messages.messageDeleted'))
  } catch (error: any) {
    if (error !== 'cancel') {
      uiStore.showError(error.message || '删除失败')
    }
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await messageApi.markAllAsReadApi()
    messages.value.forEach((m) => {
      m.isRead = true
    })
    uiStore.showSuccess(t('messages.allMarkedAsRead'))
  } catch (error: any) {
    uiStore.showError(error.message || '操作失败')
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <h2>{{ t('messages.title') }}</h2>
          <el-tag v-if="unreadCount > 0" type="danger" size="small">
            {{ unreadCount }} {{ t('statistics.unreadMessages') }}
          </el-tag>
        </div>
        <div class="header-actions">
          <el-button
            v-if="unreadCount > 0"
            type="primary"
            size="small"
            @click="handleMarkAllAsRead"
          >
            {{ t('messages.markAllRead') }}
          </el-button>
          <el-button
            :loading="refreshLoading"
            :icon="refreshLoading ? undefined : 'Refresh'"
            size="small"
            @click="handleRefresh"
          >
            {{ t('common.refresh') }}
          </el-button>
        </div>
      </div>
    </template>

    <div v-if="messages.length > 0" class="messages-container">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="{ unread: !message.isRead }"
      >
        <div class="message-content">
          <div class="message-header">
            <el-tag :type="getTagType(message.type)" size="small">
              {{ getTypeText(message.type) }}
            </el-tag>
            <span class="message-time">{{ formatTime(message.createdAt) }}</span>
          </div>
          <div class="message-title">{{ message.title }}</div>
        </div>
        <div class="message-actions">
          <el-button
            :icon="message.isRead ? 'View' : 'View'"
            size="small"
            text
            @click="handleToggleRead(message)"
          >
            {{ message.isRead ? t('messages.markAsUnread') : t('messages.markAsRead') }}
          </el-button>
          <el-button
            type="danger"
            :icon="'Delete'"
            size="small"
            text
            @click="handleDelete(message)"
          >
            {{ t('messages.delete') }}
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else :description="t('messages.noMessages')" />
  </el-card>
</template>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
  }

  &.unread {
    background: #ecf5ff;
    border-left: 3px solid #409eff;

    .message-title {
      font-weight: 600;
      color: #303133;
    }
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-title {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}
</style>

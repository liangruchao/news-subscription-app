<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { messageApi } from '@/api'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const announcements = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    announcements.value = await messageApi.getAnnouncementsApi()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <el-card>
    <template #header>
      <h2>{{ t('announcements.title') }}</h2>
    </template>

    <div v-loading="loading" v-if="announcements.length > 0">
      <el-timeline>
        <el-timeline-item
          v-for="item in announcements"
          :key="item.id"
          :timestamp="new Date(item.createdAt).toLocaleString()"
        >
          <h4>{{ item.title }}</h4>
          <p>{{ item.content }}</p>
          <el-tag :type="item.priority === 'high' ? 'danger' : item.priority === 'medium' ? 'warning' : 'info'">
            {{ t(`announcements.priority.${item.priority}`) }}
          </el-tag>
        </el-timeline-item>
      </el-timeline>
    </div>

    <el-empty v-else :description="t('announcements.noAnnouncements')" />
  </el-card>
</template>

<style scoped lang="scss">
h2 { margin: 0; }
h4 { margin: 0 0 8px 0; }
p { color: #666; }
</style>

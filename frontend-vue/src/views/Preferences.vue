<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUiStore } from '@/stores/ui'
import { userApi } from '@/api'
import { setLanguage } from '@/i18n'
import type { UserPreferences } from '@/api'

const { t } = useI18n()
const uiStore = useUiStore()

const loading = ref(false)
const saveLoading = ref(false)

const form = reactive<UserPreferences>({
  newsNotification: true,
  systemNotification: true,
  subscriptionNotification: true,
  newsPageSize: 10,
  compactMode: false,
  interfaceLanguage: 'zh-CN',
  publicProfile: false,
  showOnlineStatus: true,
})

const pageSizeOptions = computed(() => [
  { label: t('preferences.pageSize5'), value: 5 },
  { label: t('preferences.pageSize10'), value: 10 },
  { label: t('preferences.pageSize15'), value: 15 },
  { label: t('preferences.pageSize20'), value: 20 },
])

const languageOptions = computed(() => [
  { label: t('preferences.languageZh'), value: 'zh-CN' },
  { label: t('preferences.languageEn'), value: 'en-US' },
])

// 获取当前偏好设置
const fetchPreferences = async () => {
  loading.value = true
  try {
    console.log('[Preferences] 正在获取用户偏好设置...')
    const prefs = await userApi.getUserPreferencesApi()
    console.log('[Preferences] 获取到偏好设置:', prefs)
    Object.assign(form, prefs)
    uiStore.showSuccess(t('preferences.loaded'))
  } catch (error: any) {
    console.error('[Preferences] 获取偏好设置失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || t('preferences.loadFailed')
    uiStore.showError(errorMsg)
  } finally {
    loading.value = false
  }
}

// 保存偏好设置
const handleSave = async () => {
  saveLoading.value = true
  try {
    console.log('[Preferences] 正在保存偏好设置:', form)
    await userApi.updateUserPreferencesApi(form)
    console.log('[Preferences] 保存成功')

    uiStore.showSuccess(t('preferences.saved'))

    // 如果修改了语言，切换语言
    if (form.interfaceLanguage) {
      console.log('[Preferences] 切换语言到:', form.interfaceLanguage)
      setLanguage(form.interfaceLanguage as 'zh-CN' | 'en-US')
    }
  } catch (error: any) {
    console.error('[Preferences] 保存失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || t('preferences.saveFailed')
    uiStore.showError(errorMsg)
  } finally {
    saveLoading.value = false
  }
}

// 重置为默认设置
const handleReset = async () => {
  const defaultPrefs: UserPreferences = {
    newsNotification: true,
    systemNotification: true,
    subscriptionNotification: true,
    newsPageSize: 10,
    compactMode: false,
    interfaceLanguage: 'zh-CN',
    publicProfile: false,
    showOnlineStatus: true,
  }

  console.log('[Preferences] 重置为默认设置:', defaultPrefs)
  Object.assign(form, defaultPrefs)
  await handleSave()
}

// 语言切换
const handleLanguageChange = (lang: string) => {
  console.log('[Preferences] 语言切换到:', lang)
  setLanguage(lang as 'zh-CN' | 'en-US')
}

onMounted(() => {
  console.log('[Preferences] 组件已挂载，开始加载偏好设置')
  fetchPreferences()
})
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="card-header">
        <h2>{{ t('preferences.title') }}</h2>
      </div>
    </template>

    <el-form label-width="150px" style="max-width: 700px">
      <!-- 通知设置 -->
      <el-divider content-position="left">
        <el-icon><Bell /></el-icon>
        <span>{{ t('preferences.notificationSettings') }}</span>
      </el-divider>

      <el-form-item :label="t('preferences.newsNotification')">
        <el-switch v-model="form.newsNotification" />
        <span class="form-desc">{{ t('preferences.newsNotificationDesc') }}</span>
      </el-form-item>

      <el-form-item :label="t('preferences.systemNotification')">
        <el-switch v-model="form.systemNotification" />
        <span class="form-desc">{{ t('preferences.systemNotificationDesc') }}</span>
      </el-form-item>

      <el-form-item :label="t('preferences.subscriptionNotification')">
        <el-switch v-model="form.subscriptionNotification" />
        <span class="form-desc">{{ t('preferences.subscriptionNotificationDesc') }}</span>
      </el-form-item>

      <!-- 显示设置 -->
      <el-divider content-position="left" style="margin-top: 30px">
        <el-icon><Monitor /></el-icon>
        <span>{{ t('preferences.displaySettings') }}</span>
      </el-divider>

      <el-form-item :label="t('preferences.newsPageSize')">
        <el-select v-model="form.newsPageSize" style="width: 200px">
          <el-option
            v-for="option in pageSizeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <span class="form-desc">{{ t('preferences.newsPageSizeDesc') }}</span>
      </el-form-item>

      <el-form-item :label="t('preferences.compactMode')">
        <el-switch v-model="form.compactMode" />
        <span class="form-desc">{{ t('preferences.compactModeDesc') }}</span>
      </el-form-item>

      <!-- 语言设置 -->
      <el-divider content-position="left" style="margin-top: 30px">
        <el-icon><Reading /></el-icon>
        <span>{{ t('preferences.languageSettings') }}</span>
      </el-divider>

      <el-form-item :label="t('preferences.interfaceLanguage')">
        <el-radio-group
          v-model="form.interfaceLanguage"
          @change="() => handleLanguageChange(form.interfaceLanguage)"
        >
          <el-radio-button
            v-for="option in languageOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
        <span class="form-desc">{{ t('preferences.interfaceLanguageDesc') }}</span>
      </el-form-item>

      <!-- 隐私设置 -->
      <el-divider content-position="left" style="margin-top: 30px">
        <el-icon><Lock /></el-icon>
        <span>{{ t('preferences.privacySettings') }}</span>
      </el-divider>

      <el-form-item :label="t('preferences.publicProfile')">
        <el-switch v-model="form.publicProfile" />
        <span class="form-desc">{{ t('preferences.publicProfileDesc') }}</span>
      </el-form-item>

      <el-form-item :label="t('preferences.showOnlineStatus')">
        <el-switch v-model="form.showOnlineStatus" />
        <span class="form-desc">{{ t('preferences.showOnlineStatusDesc') }}</span>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-divider style="margin-top: 40px" />

      <el-form-item>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">
          {{ t('common.save') }}
        </el-button>
        <el-button @click="handleReset">
          {{ t('preferences.resetToDefault') }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped lang="scss">
.card-header {
  h2 { margin: 0; }
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
}

.form-desc {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>

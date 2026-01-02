import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

export interface MessageConfig {
  message: string
  type?: 'success' | 'warning' | 'info' | 'error'
  duration?: number
}

export const useUiStore = defineStore('ui', () => {
  // State
  const loading = ref(false)
  const pageLoading = ref(false)

  // Actions
  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const setPageLoading = (value: boolean) => {
    pageLoading.value = value
  }

  const showMessage = ({ message, type = 'success', duration = 3000 }: MessageConfig) => {
    ElMessage({
      message,
      type,
      duration,
    })
  }

  const showSuccess = (message: string) => {
    showMessage({ message, type: 'success' })
  }

  const showError = (message: string) => {
    showMessage({ message, type: 'error' })
  }

  const showWarning = (message: string) => {
    showMessage({ message, type: 'warning' })
  }

  const showInfo = (message: string) => {
    showMessage({ message, type: 'info' })
  }

  return {
    // State
    loading,
    pageLoading,
    // Actions
    setLoading,
    setPageLoading,
    showMessage,
    showSuccess,
    showError,
    showWarning,
    showInfo,
  }
})

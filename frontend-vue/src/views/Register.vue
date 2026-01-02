<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const validateEmail = (_rule: any, value: string, callback: any) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) {
    callback(new Error(t('auth.emailRequired')))
  } else if (!emailRegex.test(value)) {
    callback(new Error(t('auth.invalidEmail')))
  } else {
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error(t('auth.passwordRequired')))
  } else if (value !== form.password) {
    callback(new Error(t('profile.passwordMismatch')))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: () => t('auth.usernameRequired'), trigger: 'blur' },
    { min: 4, message: () => t('auth.usernameTooShort'), trigger: 'blur' },
  ],
  email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordTooShort'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const result = await authStore.register({
    username: form.username,
    email: form.email,
    password: form.password,
  })
  loading.value = false

  if (result.success) {
    uiStore.showSuccess(t('auth.registerSuccess'))
    router.push('/')
  } else {
    uiStore.showError(result.message || t('auth.registerFailed'))
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>{{ t('auth.register') }}</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="t('auth.username')" prop="username">
          <el-input v-model="form.username" clearable />
        </el-form-item>

        <el-form-item :label="t('auth.email')" prop="email">
          <el-input v-model="form.email" clearable />
        </el-form-item>

        <el-form-item :label="t('auth.password')" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-form-item :label="t('auth.confirmPassword')" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">
            {{ t('auth.register') }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="footer-links">
            <span>{{ t('auth.hasAccount') }}</span>
            <el-link type="primary" @click="goToLogin">{{ t('auth.toLogin') }}</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
  border-radius: 12px;
}

.card-header {
  text-align: center;
  h2 { margin: 0; }
}

.footer-links {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
}
</style>

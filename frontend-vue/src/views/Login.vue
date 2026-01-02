<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const uiStore = useUiStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

const rules: FormRules = {
  username: [
    { required: true, message: () => t('auth.usernameRequired'), trigger: 'blur' },
    { min: 4, message: () => t('auth.usernameTooShort'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordTooShort'), trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const result = await authStore.login({
    username: form.username,
    password: form.password,
  })
  loading.value = false

  if (result.success) {
    uiStore.showSuccess(t('auth.loginSuccess'))
    // 跳转到原来的页面或首页
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } else {
    uiStore.showError(result.message || t('auth.loginFailed'))
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>{{ t('auth.login') }}</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item :label="t('auth.username')" prop="username">
          <el-input
            v-model="form.username"
            :placeholder="t('auth.usernameRequired')"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item :label="t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t('auth.passwordRequired')"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.rememberMe">
            {{ t('auth.rememberMe') }}
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            {{ t('auth.login') }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="footer-links">
            <span>{{ t('auth.noAccount') }}</span>
            <el-link type="primary" @click="goToRegister">
              {{ t('auth.toRegister') }}
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;

  h2 {
    margin: 0;
    color: #333;
    font-weight: 500;
  }
}

.footer-links {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}
</style>

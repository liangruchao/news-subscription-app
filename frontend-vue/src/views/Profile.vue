<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { userApi } from '@/api'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()
const { t } = useI18n()

const activeTab = ref('profile')
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 个人资料表单
const profileForm = reactive({
  username: authStore.username || '',
  email: authStore.email || '',
  bio: '',
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileLoading = ref(false)
const passwordLoading = ref(false)
const uploadLoading = ref(false)

// 表单验证规则
const profileRules: FormRules = {
  email: [
    { required: true, message: () => t('auth.emailRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) {
          callback(new Error(t('auth.invalidEmail')))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  bio: [
    { max: 500, message: '个人简介不能超过500个字符', trigger: 'blur' },
  ],
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error(t('auth.passwordRequired')))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error(t('profile.passwordMismatch')))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordTooShort'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

// 保存个人资料
const handleSaveProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    await userApi.updateUserProfileApi({
      bio: profileForm.bio,
    })
    // 更新本地用户信息
    await authStore.fetchCurrentUser()
    uiStore.showSuccess(t('profile.profileUpdated'))
  } catch (error: any) {
    uiStore.showError(error.message || t('profile.updateFailed'))
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await userApi.changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })

    uiStore.showSuccess(t('profile.passwordChanged'))

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.resetFields()
  } catch (error: any) {
    uiStore.showError(error.message || t('profile.passwordChangeFailed'))
  } finally {
    passwordLoading.value = false
  }
}

// 上传头像
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    uiStore.showError('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    uiStore.showError('图片大小不能超过 10MB！')
    return false
  }
  return true
}

const handleUpload = async (file: File) => {
  uploadLoading.value = true
  try {
    await userApi.uploadAvatarApi(file)
    // 更新本地用户信息
    await authStore.fetchCurrentUser()
    uiStore.showSuccess('头像上传成功')
  } catch (error: any) {
    uiStore.showError(error.message || '头像上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// Upload request handler for el-upload
const handleUploadRequest = (options: any) => {
  handleUpload(options.file as File)
}

// 注销账户
const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      t('profile.deleteAccountConfirm'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
    )

    await userApi.deleteAccountApi()
    uiStore.showSuccess(t('profile.accountDeleted'))

    // 登出并跳转到登录页
    await authStore.logout()
    router.push('/login')
  } catch (error: any) {
    if (error !== 'cancel') {
      uiStore.showError(error.message || t('profile.accountDeleteFailed'))
    }
  }
}
</script>

<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <h2>{{ t('profile.title') }}</h2>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <!-- 基本信息 -->
      <el-tab-pane :label="t('profile.basicInfo')" name="profile">
        <div class="profile-section">
          <!-- 头像上传 -->
          <div class="avatar-section">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUploadRequest"
              :disabled="uploadLoading"
            >
              <el-avatar v-if="authStore.avatarUrl" :src="authStore.avatarUrl" :size="100" />
              <el-avatar v-else :size="100" icon="User" />
              <div v-if="!uploadLoading" class="avatar-overlay">
                <el-icon><Camera /></el-icon>
                <span>{{ t('profile.changeAvatar') }}</span>
              </div>
              <div v-else class="avatar-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </el-upload>
          </div>

          <!-- 基本信息表单 -->
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            style="max-width: 500px"
          >
            <el-form-item :label="t('profile.username')">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item :label="t('profile.email')" prop="email">
              <el-input v-model="profileForm.email" disabled />
            </el-form-item>

            <el-form-item :label="t('profile.bio')" prop="bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                :placeholder="t('profile.bio')"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="profileLoading"
                @click="handleSaveProfile"
              >
                {{ t('profile.saveProfile') }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane :label="t('profile.changePassword')" name="password">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          style="max-width: 400px"
        >
          <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item :label="t('profile.newPassword')" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="passwordLoading"
              @click="handleChangePassword"
            >
              {{ t('profile.changePassword') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 账户安全 -->
      <el-tab-pane label="账户安全" name="security">
        <el-alert
          title="危险操作"
          type="error"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <p>注销账户后，您的所有数据将被永久删除，此操作不可恢复！</p>
          <el-button
            type="danger"
            plain
            @click="handleDeleteAccount"
          >
            {{ t('profile.deleteAccount') }}
          </el-button>
        </el-alert>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped lang="scss">
.card-header {
  h2 { margin: 0; }
}

.profile-section {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  padding: 20px 0;

  .el-upload {
    position: relative;
    cursor: pointer;
    border-radius: 50%;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      .avatar-overlay {
        opacity: 1;
      }
    }
  }

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    opacity: 0;
    transition: opacity 0.3s;
    font-size: 12px;
    gap: 4px;
  }

  .avatar-loading {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    color: #409eff;
    font-size: 24px;
  }
}
</style>

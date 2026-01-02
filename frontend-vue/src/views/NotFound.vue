<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>{{ t('notFound.title') }}</h2>
      <p>{{ t('notFound.description') }}</p>
      <el-button type="primary" @click="router.push('/')">
        {{ t('notFound.backHome') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.content {
  text-align: center;

  h1 {
    font-size: 80px;
    margin: 0;
    color: #409eff;
  }

  h2 { margin: 20px 0 10px; }
  p { color: #999; margin-bottom: 30px; }
}
</style>

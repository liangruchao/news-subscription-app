<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { messageApi } from '@/api'

const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()

// 判断是否显示导航栏（登录页和注册页不显示）
const showNav = computed(() => {
  return !route.meta?.guest
})

// 未读消息数量
const unreadCount = ref(0)

// 获取未读消息数量
const fetchUnreadCount = async () => {
  // 确保用户已登录才调用 API
  if (!authStore.isLoggedIn) return

  try {
    const result = await messageApi.getUnreadCountApi()
    unreadCount.value = result.count
  } catch (error) {
    // 静默处理错误，可能是 session 过期
    // console.error('获取未读消息数量失败:', error)
  }
}

// 页面可见性变化时更新未读数量
const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    fetchUnreadCount()
  }
}

onMounted(() => {
  if (authStore.isLoggedIn) {
    fetchUnreadCount()
    // 每30秒刷新一次未读数量
    setInterval(fetchUnreadCount, 30000)
  }
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <div class="app-container">
    <!-- 导航栏 -->
    <header v-if="showNav" class="app-header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <h1>📰 {{ t('common.title') }}</h1>
          </router-link>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-link">
            {{ t('nav.home') }}
          </router-link>
          <router-link to="/articles" class="nav-link">
            {{ t('nav.articles') }}
          </router-link>
          <router-link to="/rss-feeds" class="nav-link">
            {{ t('nav.rssFeeds') }}
          </router-link>
          <router-link to="/favorites" class="nav-link">
            {{ t('nav.favorites') }}
          </router-link>
          <router-link to="/search" class="nav-link">
            {{ t('nav.search') }}
          </router-link>
          <router-link to="/profile" class="nav-link">
            {{ t('nav.profile') }}
          </router-link>
          <router-link to="/preferences" class="nav-link">
            {{ t('nav.preferences') }}
          </router-link>
          <router-link to="/messages" class="nav-link nav-link-with-badge">
            {{ t('nav.messages') }}
            <el-badge
              v-if="unreadCount > 0"
              :value="unreadCount > 99 ? '99+' : unreadCount"
              class="nav-badge"
              type="danger"
            />
          </router-link>
          <router-link to="/announcements" class="nav-link">
            {{ t('nav.announcements') }}
          </router-link>
          <a href="#" class="nav-link" @click.prevent="authStore.logout()">
            {{ t('nav.logout') }}
          </a>
        </nav>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer v-if="showNav" class="app-footer">
      <p>{{ t('common.footer') }}</p>
    </footer>
  </div>
</template>

<style scoped lang="scss">
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo a {
  text-decoration: none;
  color: #333;

  h1 {
    font-size: 20px;
    margin: 0;
  }
}

.nav-menu {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    color: #409eff;
    background: #f0f7ff;
  }

  &.router-link-active {
    color: #409eff;
    font-weight: 500;
  }

  &.nav-link-with-badge {
    position: relative;
  }

  :deep(.el-badge) {
    .el-badge__content {
      font-size: 10px;
      height: 16px;
      line-height: 16px;
      padding: 0 4px;
      border: 1px solid #fff;
    }
  }
}

.nav-badge {
  transform: translateY(-2px);
}

.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.app-footer {
  background: #f5f5f5;
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-top: auto;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

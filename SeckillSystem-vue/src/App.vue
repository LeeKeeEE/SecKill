<template>
  <header>
    <div class="branding">
      <img alt="Vue logo" class="logo" src="@/assets/Miku_logo.png" width="90" height="120" />
      <div class="wrapper">
        <nav>
          <RouterLink to="/">首页（活动列表）</RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/orders">我的订单</RouterLink>
        </nav>
      </div>
    </div>

    <div class="user-nav">
      <div v-if="authStore.isLoggedIn" class="user-info">
        <span>欢迎, {{ authStore.username }}</span>
        <a @click="handleLogout" class="logout-button">退出登录</a>
      </div>
      <div v-else class="guest-info">
        <RouterLink to="/login">登录</RouterLink>
        <RouterLink to="/register">注册</RouterLink>
      </div>
    </div>
  </header>

  <RouterView />
</template>

<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router';
// 1. 引入 authStore
import { useAuthStore } from '@/stores/authStore';

// 2. 获取 store 和 router 实例
const authStore = useAuthStore();
const router = useRouter();

// 3. 定义退出登录的处理函数
const handleLogout = () => {
  authStore.logout();
  alert('您已成功退出登录。');
  router.push('/login');
};
</script>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
  /* 关键修改：使用 flex布局 来分布元素 */
  display: flex;
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
  padding: 0.5rem 2rem; /* 调整内边距 */
  border-bottom: 1px solid var(--color-border);
}

/* 新增一个容器来包裹 logo 和主导航 */
.branding {
  display: flex;
  align-items: center;
}

.logo {
  display: block;
  margin: 0 2rem 0 0;
}

nav {
  width: 120%;
  font-size: 14px;
  text-align: center;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

/* --- 新增的用户导航区域样式 --- */
.user-nav {
  /* 这个容器将显示在右上角 */
}

.user-info, .guest-info {
  display: flex;
  align-items: center;
  gap: 1.5rem; /* 元素间距 */
  font-size: 1rem;
}

.user-info span {
  font-weight: bold;
}

.logout-button {
  color: #dc3545;
  cursor: pointer;
}
.logout-button:hover {
  text-decoration: underline;
}


/* 媒体查询调整 */
@media (min-width: 1024px) {
  header {
    padding-right: 0;
  }

  .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;
    padding: 1rem 0;
    margin-top: 0;
  }
}
</style>

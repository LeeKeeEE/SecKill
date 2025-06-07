import { defineStore } from 'pinia';
import type { User } from '@/types'; // 从你的 types/index.ts 导入 User 类型

// 定义 state 的类型
export interface AuthState {
  user: User | null;
  token: string | null;
}

export const useAuthStore = defineStore('auth', {
  // 为了在页面刷新后保持登录状态，我们尝试从 localStorage 初始化 state
  state: (): AuthState => {
    const user = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    return {
      user: user ? JSON.parse(user) : null,
      token: token || null,
    };
  },

  getters: {
    // 创建一个 getter 判断用户是否已登录
    isLoggedIn: (state) => !!state.user && !!state.token,
    // 获取用户ID，如果未登录则返回 null
    userId: (state) => state.user?.id || null,
    // 获取用户名
    username: (state) => state.user?.username || '游客',
  },

  actions: {
    // 登录成功后，调用这个 action 来设置用户状态
    setAuthInfo({ user, token }: { user: User, token: string }) {
      this.user = user;
      this.token = token;
      // 将用户信息和token存入 localStorage，以便持久化
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('token', token);
    },

    // 退出登录时调用
    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      // 退出后可以重定向到登录页
      // this.router.push('/login');
    },
  },
});

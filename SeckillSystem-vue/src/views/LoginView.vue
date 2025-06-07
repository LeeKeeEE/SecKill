<template>
  <div class="login-container">
    <div class="login-card">
      <h3>用户登录</h3>

      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="phone">手机号</label>
          <input
            type="text"
            id="phone"
            v-model="phone"
            placeholder="请输入手机号"
            required
            minlength="11"
            :disabled="isLoading"
          />
          <small v-if="validationErrors.phone" class="form-error">{{ validationErrors.phone }}</small>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="password"
            placeholder="请输入密码"
            required
            minlength="5"
            :disabled="isLoading"
          />
          <small v-if="validationErrors.password" class="form-error">{{ validationErrors.password }}</small>
        </div>

        <div class="form-group">
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            {{ isLoading ? '登录中...' : '登录' }}
          </button>
          <router-link to="/register" class="btn btn-link">还没有账号？去注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; // 已存在
import apiClient from '@/api/axios'; // 已存在
import md5 from 'blueimp-md5'; // 已存在

const phone = ref('');
const password = ref('');
const errorMessage = ref<string | null>(null);
const isLoading = ref(false);
const authStore = useAuthStore();
const router = useRouter();

const validationErrors = ref<{ phone?: string; password?: string }>({});

// 前端 MD5 加盐处理 (与 login.html 中的逻辑保持一致)
const JUMP_SALT = '1a2b3c4d';
const prePass = (inputPass: string) => {
  const str = "" + JUMP_SALT.charAt(0) + JUMP_SALT.charAt(2) + inputPass + JUMP_SALT.charAt(5) + JUMP_SALT.charAt(4);
  return md5(str);
};

const validateForm = (): boolean => {
  validationErrors.value = {};
  let isValid = true;

  if (!phone.value) {
    validationErrors.value.phone = '请输入手机号';
    isValid = false;
  } else if (phone.value.length !== 11 || !/^\d{11}$/.test(phone.value)) {
    validationErrors.value.phone = '请输入有效的11位手机号';
    isValid = false;
  }

  if (!password.value) {
    validationErrors.value.password = '请输入密码';
    isValid = false;
  } else if (password.value.length < 5) {
    validationErrors.value.password = '密码长度至少为5位';
    isValid = false;
  }
  return isValid;
};

const handleLogin = async () => {
  errorMessage.value = null;
  if (!validateForm()) {
    return;
  }

  isLoading.value = true;
  const formPassword = prePass(password.value);

  try {
    const response = await apiClient.post('/login/do_login', {
      phone: phone.value,
      password: formPassword,
    });

    // --- 主要修改在这里 ---
    if (response.data && response.data.code === 0) {
      // 假设后端成功时返回 { code: 0, data: { user: User, token: string } }
      const loginResult = response.data.data;

      if (loginResult && loginResult.user && loginResult.token) {
        // 调用 authStore 的 action 来保存用户状态
        authStore.setAuthInfo({
          user: loginResult.user,
          token: loginResult.token
        });

        // 登录成功后跳转到首页
        await router.push('/');
      } else {
        // 如果后端返回 code 0 但数据结构不符
        errorMessage.value = '登录响应数据格式不正确。';
      }
    } else {
      errorMessage.value = response.data.msg || '登录失败，请检查手机号或密码。';
    }
  } catch (error: any) {
    console.error('Login API error:', error);
    if (error.response && error.response.data && error.response.data.msg) {
      errorMessage.value = error.response.data.msg;
    } else {
      errorMessage.value = '登录请求失败，请检查网络连接或稍后再试。';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-card {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

.login-card h3 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #555555;
  font-weight: bold;
}

.login-form .form-group {
  margin-bottom: 1.5rem;
}

.login-form label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555555;
  font-weight: bold;
}

.login-form input[type="text"],
.login-form input[type="password"] {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 1rem;
}

.login-form input[type="text"]:focus,
.login-form input[type="password"]:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.login-form .form-error {
  display: block;
  color: red;
  font-size: 0.875em;
  margin-top: 0.25rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background-color: #00d2d6;
  color: white;
  width: 100%;
  margin-bottom: 1rem;
}

.btn-primary:hover {
  background-color: #8fffff;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-link {
  color: #009696;
  background-color: transparent;
  text-decoration: none;
  display: block;
  text-align: center;
}

.btn-link:hover {
  text-decoration: underline;
}

.alert {
  padding: 1rem;
  margin-bottom: 1rem;
  border: 1px solid transparent;
  border-radius: 0.25rem;
}

.alert-danger {
  color: #721c24;
  background-color: #f8d7da;
  border-color: #f5c6cb;
  text-align: center;
}
</style>

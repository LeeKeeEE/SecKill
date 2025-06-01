<template>
  <div class="register-container">
    <div class="register-card">
      <h3>新用户注册</h3>

      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
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
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="username"
            placeholder="请输入用户名"
            required
            minlength="2"
            :disabled="isLoading"
          />
          <small v-if="validationErrors.username" class="form-error">{{ validationErrors.username }}</small>
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
          <button type="submit" class="btn btn-success" :disabled="isLoading">
            {{ isLoading ? '注册中...' : '注册' }}
          </button>
          <router-link to="/login" class="btn btn-link">已有账号？去登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';

const phone = ref('');
const username = ref('');
const password = ref('');
const errorMessage = ref<string | null>(null);
const isLoading = ref(false);
const router = useRouter();

const validationErrors = ref<{ phone?: string; username?: string; password?: string }>({});

const validateForm = (): boolean => {
  validationErrors.value = {};
  let isValid = true;

  if (!phone.value) {
    validationErrors.value.phone = '请输入手机号';
    isValid = false;
  } else if (phone.value.length < 11) {
    validationErrors.value.phone = '手机号长度至少为11位';
    isValid = false;
  } else if (!/^\d{11}$/.test(phone.value)) { // 简单的11位数字校验
     validationErrors.value.phone = '手机号格式不正确';
     isValid = false;
  }


  if (!username.value) {
    validationErrors.value.username = '请输入用户名';
    isValid = false;
  } else if (username.value.length < 2) {
    validationErrors.value.username = '用户名长度至少为2位';
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

const handleRegister = async () => {
  errorMessage.value = null;
  if (!validateForm()) {
    return;
  }

  isLoading.value = true;

  // 后端 RegisterController @PostMapping("/register") 接收 @RequestParam
  // 通常表单提交方式是 application/x-www-form-urlencoded
  const params = new URLSearchParams();
  params.append('phone', phone.value);
  params.append('username', username.value);
  params.append('password', password.value);

  try {

    const response = await apiClient.post('/register', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });

    // 如果没有错误，且返回状态码是2xx，就认为成功
    if (response.status >= 200 && response.status < 300) {
        alert('注册成功！将跳转到登录页面。'); // 临时提示
        router.push('/login');
    } else {
        // 尝试从响应中获取错误信息
        errorMessage.value = (response.data && response.data.message) || response.data || '注册失败，手机或用户名可能已存在。';
    }

  } catch (error: any) {
    console.error('Registration failed:', error);
    if (error.response && error.response.data) {
      errorMessage.value = '注册失败，手机或用户名可能已存在。';
    } else {
      errorMessage.value = '注册请求失败，请检查网络连接或稍后再试。';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.register-card {
  background-color: hsl(0, 0%, 100%);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

.register-card h3 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #0a615f;
  font-weight: bold;
}

.register-form .form-group {
  margin-bottom: 1.5rem;
}

.register-form label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: bold;
}

.register-form input[type="text"],
.register-form input[type="password"] {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 1rem;
}

.register-form input[type="text"]:focus,
.register-form input[type="password"]:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.register-form .form-error {
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

.btn-success {
  background-color: #00613d;
  color: white;
  width: 100%;
  margin-bottom: 1rem;
}

.btn-success:hover {
  background-color: #218838;
}

.btn-success:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-link {
  color: #007bff;
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

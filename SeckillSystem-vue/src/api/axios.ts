// src/api/index.ts 或者 src/api/axios.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // 你的 Spring Boot 后端 API 基础路径
  headers: {
    'Content-Type': 'application/json',
    // 如果有token，可以在这里统一处理或通过拦截器添加
  }
});

// 可选：添加请求拦截器 (例如：统一添加 token)
apiClient.interceptors.request.use(
  (config) => {
    // const token = localStorage.getItem('user-token'); // 假设 token 存储在 localStorage
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 可选：添加响应拦截器 (例如：统一处理错误)
apiClient.interceptors.response.use(
  (response) => {
    return response; // 直接返回响应数据部分 response.data 也可以
  },
  (error) => {
    // 处理 HTTP 错误
    // 例如：如果 401 未授权，则跳转到登录页
    // if (error.response && error.response.status === 401) {
    //   router.push('/login'); // 需要导入 router
    // }
    console.error('API Error:', error.response || error.message);
    return Promise.reject(error);
  }
);

export default apiClient;

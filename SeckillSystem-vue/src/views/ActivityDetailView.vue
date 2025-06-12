<template>
  <div class="container">
    <div v-if="isLoading" class="loading">正在加载活动详情...</div>
    <div v-else-if="errorMessage" class="error">
      <h2>加载失败</h2>
      <p>{{ errorMessage }}</p>
    </div>
    <div v-else-if="activity" class="activity-detail">
      <h1>{{ activity.name }}</h1>

      <div v-if="activity.productVo" class="product-info">
        <h2>商品: {{ activity.productVo.name }}</h2>
        <img v-if="activity.productVo.image" :src="activity.productVo.image" :alt="activity.productVo.name" class="product-image" />
        <p v-if="activity.productVo.description">{{ activity.productVo.description }}</p>
        <p>原价: ¥{{ activity.productVo.price.toFixed(2) }}</p>
        <p>当前剩余: {{ activity.productVo.stock }}</p>
      </div>

      <div class="seckill-info">
        <h3>秒杀详情</h3>
        <p class="seckill-price">秒杀价: ¥{{ activity.seckillPrice.toFixed(2) }}</p>
        <p>活动仅剩: {{ activity.stockCount }} 件</p>
        <p>开始时间: {{ new Date(activity.startTime).toLocaleString() }}</p>
        <p>结束时间: {{ new Date(activity.endTime).toLocaleString() }}</p>
        <p>状态: <span :class="statusClass">{{ getActivityStatusText(activity.status) }}</span></p>
      </div>

      <div class="seckill-action">
        <button
          @click="handleSeckill"
          :disabled="activity.status !== 1 || seckillInProgress"
          class="seckill-button"
        >
          {{ seckillInProgress ? '处理中...' : '立即秒杀' }}
        </button>
        <div v-if="seckillMessage" :class="seckillResultIsError ? 'error-message' : 'success-message'">
          {{ seckillMessage }}
        </div>
      </div>
    </div>
    <div v-else class="not-found">
      <p>未找到该活动信息。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import apiClient from '@/api/axios'; // 引入你的axios实例
import type { SeckillActivityVo } from '@/types/index';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const activityId = route.params.id as string;

const activity = ref<SeckillActivityVo | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const seckillInProgress = ref(false);
const seckillMessage = ref<string | null>(null);
const seckillResultIsError = ref(false);
const authStore = useAuthStore();

// 计算属性，用于根据状态动态添加CSS类
const statusClass = computed(() => {
  if (!activity.value) return '';
  switch (activity.value.status) {
    case 1: return 'status-inprogress';
    case 2: return 'status-ended';
    default: return 'status-notstarted';
  }
});

// 获取活动状态的文本描述
function getActivityStatusText(status: number): string {
  switch (status) {
    case 0: return '未开始';
    case 1: return '进行中';
    case 2: return '已结束';
    default: return '未知';
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  if (!activityId) {
    errorMessage.value = '未提供活动ID。';
    isLoading.value = false;
    return;
  }
  try {
    const response = await apiClient.get<SeckillActivityVo>(`/seckill/activity/${activityId}`);
    activity.value = response.data;
  } catch (err: any) {
    if (err.response && err.response.data) {
      // 处理后端返回的 Result 对象
      errorMessage.value = err.response.data.msg || '加载活动失败。';
    } else {
      errorMessage.value = '网络请求错误，请稍后重试。';
    }
    console.error(`获取活动 ${activityId} 失败:`, err);
  } finally {
    isLoading.value = false;
  }
});

// 处理秒杀按钮点击事件
async function handleSeckill() {
  if (!activity.value || !activity.value.productVo) {
    seckillMessage.value = '活动数据不完整，无法秒杀。';
    seckillResultIsError.value = true;
    return;
  }

  const userId = authStore.user?.id;
  // 检查用户是否登录
  if (!userId) {
    seckillMessage.value = '请先登录后再进行秒杀！';
    return;
  }

  seckillInProgress.value = true;
  seckillMessage.value = null;

  try {
    const result = await apiClient.post('/seckill/doSeckill', null, {
      params: {
        userId: userId,
        productId: activity.value.productVo.id,
        activityId: activity.value.id,
      }
    });

    if (result.data && result.data.code === 200) {
      seckillMessage.value = result.data.message || '秒杀请求成功！';
      seckillResultIsError.value = false;
    } else {
      seckillMessage.value = result.data.message || '秒杀失败！';
      seckillResultIsError.value = true;
    }
  } catch (error: any) {
    seckillMessage.value = error.response?.data?.message || '秒杀请求异常。';
    seckillResultIsError.value = true;
  } finally {
    seckillInProgress.value = false;
  }
}
</script>

<style scoped>
.container {
  padding: 2rem;
  font-family: sans-serif;
}
.loading, .error, .not-found {
  text-align: center;
  padding: 40px;
  font-size: 1.2em;
}
.error {
  color: #d9534f;
}
.activity-detail {
  max-width: 800px;
  margin: 0 auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 24px;
  background-color: #d7faf786;
}
h1 {
  text-align: center;
  margin-bottom: 24px;
}
.product-info, .seckill-info, .seckill-action {
  margin-bottom: 20px;
  background-color: #f1fffdfb;
  padding: 16px;
  border-radius: 4px;
}
.product-image {
  max-width: 100%;
  border-radius: 4px;
  margin-bottom: 15px;
}
.seckill-price {
  color: #d14927cc;
  font-size: 1.5em;
  font-weight: bold;
}
.seckill-action {
  text-align: center;
}
.seckill-button {
  background-color: #00bac7;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1.1em;
  transition: background-color 0.3s;
}
.seckill-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.seckill-button:hover:not(:disabled) {
  background-color: #6ccaddcb;
}
.error-message {
  color: #d9534f;
  margin-top: 10px;
}
.success-message {
  color: #5cb85c;
  margin-top: 10px;
}
.status-inprogress {
  color: #2cbacaa9;
  font-weight: bold;
}
.status-ended {
  color: #777;
}
.status-notstarted {
  color: #f0ad4e;
}
</style>

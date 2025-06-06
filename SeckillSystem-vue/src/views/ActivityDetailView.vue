<template>
  <div v-if="activityStore.isLoading">正在加载活动详情...</div>
  <div v-else-if="activityStore.error" class="error">
    错误: {{ activityStore.error }}
  </div>
  <div v-else-if="activity" class="activity-detail">
    <h1>{{ activity.name }}</h1>
    <div v-if="activity.product" class="product-info">
      <h2>商品: {{ activity.product.name }}</h2>
      <p v-if="activity.product.description">{{ activity.product.description }}</p>
      <p>原价: ¥{{ activity.product.price.toFixed(2) }}</p>
      <p class="seckill-price">秒杀价: ¥{{ activity.seckillPrice.toFixed(2) }}</p>
      <p>剩余库存: {{ activity.stockCount }}</p>
      <img v-if="activity.product.image" :src="activity.product.image" :alt="activity.product.name" class="product-image" />
    </div>
    <p>开始时间: {{ new Date(activity.startTime).toLocaleString() }}</p>
    <p>结束时间: {{ new Date(activity.endTime).toLocaleString() }}</p>
    <p>状态: {{ getActivityStatusText(activity.status) }}</p>

    <button
      @click="handleSeckill"
      :disabled="activity.status !== 1 || activityStore.isLoading || seckillInProgress"
      class="seckill-button"
    >
      {{ seckillInProgress ? '处理中...' : '参与秒杀' }}
    </button>
    <div v-if="seckillMessage" :class="seckillResultIsError ? 'error-message' : 'success-message'">
      {{ seckillMessage }}
    </div>
  </div>
  <div v-else>
    未找到活动。
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useActivityStore } from '@/stores/activityStore'; // 引入 activityStore
import type { SeckillActivity } from '@/types'; // 引入类型定义

const route = useRoute();
const activityStore = useActivityStore();

const activityId = computed(() => route.params.id as string);
const activity = computed(() => activityStore.currentActivity);

const seckillInProgress = ref(false);
const seckillMessage = ref<string | null>(null);
const seckillResultIsError = ref(false);

onMounted(async () => {
  if (activityId.value) {
    await activityStore.fetchActivityById(activityId.value);
  }
});

function getActivityStatusText(status: number): string {
  switch (status) {
    case 0: return '未开始';
    case 1: return '进行中';
    case 2: return '已结束';
    default: return '未知';
  }
}

async function handleSeckill() {
  if (!activity.value || !activity.value.product) {
    seckillMessage.value = '活动或商品数据缺失。';
    seckillResultIsError.value = true;
    return;
  }

  // 你需要一种方式来获取当前 userId。
  // 这可能来自另一个 store (例如，用户/认证 store) 或 localStorage。
  // 此处假设一个占位 userId。
  const placeholderUserId = 1; // 请替换为实际的用户ID逻辑

  seckillInProgress.value = true;
  seckillMessage.value = null;
  seckillResultIsError.value = false;

  try {
    const result = await activityStore.performSeckill({
      userId: placeholderUserId, // 替换为实际用户ID
      productId: activity.value.product.id,
      activityId: activity.value.id,
    });

    // 后端的 SeckillController 返回一个包含 "code" 和 "message" 的 Map<String, Object>
    if (result && result.code === 200) {
      seckillMessage.value = result.message || '秒杀请求已成功提交！';
      seckillResultIsError.value = false;
      // 可选：如果库存发生变化，你可能需要刷新活动详情
      // await activityStore.fetchActivityById(activityId.value);
    } else {
      seckillMessage.value = result.message || '秒杀失败，请重试。';
      seckillResultIsError.value = true;
    }
  } catch (error: any) {
    seckillMessage.value = error.message || '秒杀过程中发生意外错误。';
    seckillResultIsError.value = true;
  } finally {
    seckillInProgress.value = false;
  }
}
</script>

<style scoped>
.activity-detail {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.product-info {
  margin-top: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.product-image {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin-top: 10px;
}

.seckill-price {
  color: red;
  font-weight: bold;
  font-size: 1.2em;
}

.seckill-button {
  background-color: #28a745;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
  transition: background-color 0.3s;
}

.seckill-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.seckill-button:hover:not(:disabled) {
  background-color: #218838;
}

.error-message {
  color: red;
  margin-top: 10px;
}
.success-message {
  color: green;
  margin-top: 10px;
}
.error {
  color: red;
  font-weight: bold;
}
</style>

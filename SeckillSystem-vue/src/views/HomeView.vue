<template>
  <div class="home-view">
    <h1>秒杀活动列表</h1>
    <div v-if="activityStore.isLoading">加载中...</div>
    <div v-if="activityStore.error" class="error">{{ activityStore.error }}</div>
    <ul v-if="!activityStore.isLoading && !activityStore.error && activityStore.activities.length">
      <li v-for="activity in activityStore.activities" :key="activity.id" class="activity-item">
        <h2>{{ activity.name }}</h2>
        <p>商品: {{ activity.product.name }}</p>
        <p>原价: <span class="original-price">￥{{ activity.product.price }}</span></p>
        <p>秒杀价: <span class="seckill-price">￥{{ activity.seckillPrice }}</span></p>
        <p>剩余库存: {{ activity.stockCount }}</p>
        <p>开始时间: {{ formatDateTime(activity.startTime) }}</p>
        <p>结束时间: {{ formatDateTime(activity.endTime) }}</p>
        <RouterLink :to="{ name: 'activity-detail', params: { id: activity.id } }">
          <button>查看详情 &gt;</button>
        </RouterLink>
      </li>
    </ul>
    <div v-if="!activityStore.isLoading && !activityStore.error && !activityStore.activities.length">
      暂无秒杀活动。
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { useActivityStore } from '@/stores/activityStore';

const activityStore = useActivityStore();

onMounted(() => {
  activityStore.fetchActivities();
});

// 简单的日期时间格式化函数
const formatDateTime = (dateTimeString: string) => {
  if (!dateTimeString) return 'N/A';
  try {
    return new Date(dateTimeString).toLocaleString();
  } catch (e) {
    return dateTimeString; // 如果格式化失败，返回原始字符串
  }
};
</script>

<style scoped>
.home-view {
  padding: 20px;
}
.error {
  color: red;
}
.activity-item {
  border: 1px solid #eee;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 8px;
}
.activity-item h2 {
  margin-top: 0;
}
.original-price {
  text-decoration: line-through;
  color: #999;
  margin-left: 5px;
}
.seckill-price {
  color: red;
  font-weight: bold;
  font-size: 1.2em;
}
button {
  margin-top: 10px;
  padding: 8px 15px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #36a374;
}
</style>

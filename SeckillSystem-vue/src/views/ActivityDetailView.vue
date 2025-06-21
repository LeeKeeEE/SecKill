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
          :disabled="seckillInProgress || activity.status === 2"
          class="seckill-button"
        >
          {{ getButtonText() }}
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
import { ElMessageBox, ElMessage } from 'element-plus';
import { h } from 'vue';

const route = useRoute();
const activityId = route.params.id as string;

const activity = ref<SeckillActivityVo | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const seckillInProgress = ref(false);
const seckillMessage = ref<string | null>(null);
const seckillResultIsError = ref(false);
const authStore = useAuthStore();

const SuccessIcon = h(
  'svg',
  {
    viewBox: '0 0 1024 1024',
    width: '24',
    height: '24',
    fill: '#00bac7' // 图标颜色
  },
  h('path', {
    d: 'M512 0C229.2 0 0 229.2 0 512s229.2 512 512 512 512-229.2 512-512S794.8 0 512 0z m285.2 368.3L455.5 709.8c-2.3 2.8-5.5 4.4-8.9 4.4s-6.6-1.6-8.9-4.4L230.8 502.9c-4.5-5.4-3.9-13.4 1.5-17.9l36.5-30.3c5.4-4.5 13.4-3.9 17.9 1.5l160 192.3 304.8-379.2c4.9-6.1 13.5-6.7 19.3-1.6l32.6 28.3c5.8 5.1 6.4 13.7 1.5 19.4z'
  })
);
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

function getButtonText(): string {
  if (seckillInProgress.value) {
    return '处理中...';
  }
  if (activity.value) {
    switch (activity.value.status) {
      case 0: return '立即秒杀'; // 未开始时也显示“立即秒杀”
      case 1: return '立即秒杀';
      case 2: return '活动已结束';
    }
  }
  return '立即秒杀';
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
    ElMessage.error('活动数据不完整，无法秒杀。');
    return;
  }

  // 如果前端判断活动已结束，则直接提示，不发请求
  if (activity.value.status === 2) {
      ElMessage.warning('抱歉，此活动已经结束了。');
      return;
  }

  const userId = authStore.user?.id;

  // 检查用户是否登录
  if (!userId) {
    seckillMessage.value = '请先登录后再进行秒杀！';
    seckillResultIsError.value = true;
    return;
  }

  seckillInProgress.value = true;
  seckillMessage.value = null; // 清空旧消息

  try {
    const result = await apiClient.post('/seckill/doSeckill', null, {
      params: {
        userId: userId,
        productId: activity.value.productVo.id,
        activityId: activity.value.id,
      }
    });

    if (result.data && result.data.code === 200) {
      // 对于成功提交的请求
      await ElMessageBox.alert(result.data.message || '秒杀请求已提交！', '太棒了！', {
        confirmButtonText: '好的',
        icon: SuccessIcon,
        customClass: 'seckill-dialog-box',
      });
    } else {
      // 对于“未开始”、“库存不足”等业务失败
      await ElMessageBox.alert(result.data.message || '秒杀失败！', '请注意', {
        confirmButtonText: '知道了',
        type: 'warning',
        customClass: 'seckill-dialog-box', // 【核心修正】应用自定义CSS类
      });
    }
  } catch (error: any) {
    // 对于网络错误等异常
    await ElMessageBox.alert(error.response?.data?.message || '服务器开小差了...', '出错啦', {
        confirmButtonText: '关闭',
        type: 'error',
        customClass: 'seckill-dialog-box', // 【核心修正】应用自定义CSS类
    });
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
:deep(.seckill-dialog-box) {
  border-radius: 12px !important; /* 更圆润的边角 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1) !important; /* 添加柔和的阴影 */
  border: none !important; /* 去掉默认边框 */
}

/* 美化弹窗标题 */
:deep(.seckill-dialog-box .el-message-box__title) {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #333 !important;
}

/* 美化弹窗内容区域 */
:deep(.seckill-dialog-box .el-message-box__content) {
  padding: 10px 25px !important;
}

/* 美化弹窗消息文本 */
:deep(.seckill-dialog-box .el-message-box__message) {
  font-size: 16px !important;
  color: #555 !important;
}

/* 美化底部的按钮区域 */
:deep(.seckill-dialog-box .el-message-box__btns) {
  padding: 10px 25px 20px !important;
}

/* 美化确认按钮 */
:deep(.seckill-dialog-box .el-button--primary) {
  border-radius: 20px !important;
  padding: 8px 25px !important;
  font-size: 14px !important;
  background-color: #00bac7 !important; /* 使用主题色 */
  border-color: #00bac7 !important;
}

:deep(.seckill-dialog-box .el-button--primary:hover) {
  background-color: #00a1ad !important;
  border-color: #00a1ad !important;
}
</style>

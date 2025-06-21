<template>
  <div class="order-page-container">
    <h2 class="page-title">我的订单</h2>

    <div v-if="orderStore.isLoading" class="state-container">
      <p>正在加载您的订单...</p>
    </div>

    <div v-else-if="orderStore.error" class="state-container error-message">
      <p>加载订单失败: {{ orderStore.error }}</p>
    </div>

    <div v-else-if="!orderStore.orders || orderStore.orders.length === 0" class="state-container empty-state">
      <p>您还没有任何订单哦~</p>
      <router-link to="/" class="btn-go-shopping">去逛逛</router-link>
    </div>

    <div v-else class="order-list">
      <div v-for="order in orderStore.orders" :key="order.id" class="order-card">
        <div class="card-header">
          <span class="order-time">下单时间：{{ formatDateTime(order.createTime) }}</span>
          <span class="order-id">订单号：{{ order.id }}</span>
        </div>
        <div class="card-body">
          <div class="product-info">
            <img :src="order.productInfoVo.image" :alt="order.productInfoVo.name" class="product-image" />
            <div class="product-details">
              <p class="product-name">{{ order.productInfoVo.name }}</p>
              <p class="product-desc">{{ order.productInfoVo.description }}</p>
            </div>
          </div>
          <div class="order-amount">
            <p>¥ {{ order.orderAmount.toFixed(2) }}</p>
          </div>
          <div class="order-status">
            <p :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</p>
          </div>
          <div class="order-actions">
            <button class="btn-detail">订单详情</button>
            <button v-if="order.status === 0" class="btn-pay">去支付</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useOrderStore } from '@/stores/orderStore' // 确保你已创建此文件

const authStore = useAuthStore()
const orderStore = useOrderStore()
const router = useRouter()

onMounted(() => {
  if (authStore.isLoggedIn && authStore.userId !== null) {
    orderStore.fetchOrders(authStore.userId)
  } else {
    alert('请先登录以查看您的订单。')
    router.push('/login')
  }
})

const getStatusText = (status: number): string => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已支付'
    case 2: return '已取消'
    default: return '未知状态'
  }
}

const getStatusClass = (status: number): string => {
  switch (status) {
    case 0: return 'status-unpaid'
    case 1: return 'status-paid'
    case 2: return 'status-cancelled'
    default: return ''
  }
}

const formatDateTime = (dateTimeString: string): string => {
  if (!dateTimeString) return 'N/A'
  return new Date(dateTimeString).toLocaleString('zh-CN', { hour12: false })
}
</script>

<style scoped>
.order-page-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 2rem auto;
  background-color: #f4f6f8;
}

.page-title {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-weight: 500;
}

/* 状态展示 */
.state-container {
  text-align: center;
  padding: 4rem;
  background-color: #fff;
  border-radius: 8px;
  font-size: 1.1rem;
  color: #666;
}
.error-message {
  color: #d9534f;
}
.empty-state {
  border: 2px dashed #ddd;
}
.btn-go-shopping {
  display: inline-block;
  margin-top: 1rem;
  padding: 0.6rem 1.2rem;
  background-color: #00bac7;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}
.btn-go-shopping:hover {
  background-color: #008286;
}

/* 订单卡片列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.order-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
  font-size: 0.85rem;
  color: #666;
}

.card-body {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  gap: 1rem;
}

.product-info {
  display: flex;
  align-items: center;
  flex: 3;
  gap: 1rem;
}
.product-image {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}
.product-details {
  display: flex;
  flex-direction: column;
}
.product-name {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
  margin: 0 0 0.5rem 0;
}
.product-desc {
  font-size: 0.9rem;
  color: #888;
  margin: 0;
}

.order-amount, .order-status, .order-actions {
  flex: 1;
  text-align: center;
}

.order-amount p {
  font-size: 1.1rem;
  font-weight: bold;
  color: #e44d26;
  margin: 0;
}

.order-status p {
  font-weight: bold;
  margin: 0;
}
.status-paid { color: #5cb85c; }
.status-unpaid { color: #f0ad4e; }
.status-cancelled { color: #999; }

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.order-actions button {
  padding: 0.5rem 1rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
  background-color: #75decb1b;
  transition: all 0.2s;
}
.order-actions button:hover {
  border-color: #00bac7;
  color: #05601f;
}
.btn-pay {
  background-color: #f0ad4e;
  border-color: #f0ad4e;
  color: rgb(131, 206, 141);
}
.btn-pay:hover {
  background-color: #64d18c8f;
  border-color: #eea236;
  color: white;
}
</style>

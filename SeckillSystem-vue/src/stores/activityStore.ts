import { defineStore } from 'pinia'
import apiClient from '@/api/axios' // 引入apiClient
import type { SeckillActivity } from '@/types'

export const useActivityStore = defineStore('activity', {
  state: () => ({
    activities: [] as SeckillActivity[], // 活动列表
    currentActivity: null as SeckillActivity | null, // 当前查看的活动详情
    isLoading: false,
    error: null as string | null,
  }),
  actions: {
    async fetchActivities() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get<SeckillActivity[]>('/seckill/activities');
        this.activities = response.data;
      } catch (err: any) {
        this.error = err.response?.data?.message || err.message || '获取活动列表失败';
        console.error('Failed to fetch activities:', err);
      } finally {
        this.isLoading = false;
      }
    },

    async fetchActivityById(id: string | number) {
      this.isLoading = true;
      this.error = null;
      this.currentActivity = null;
      try {
        const response = await apiClient.get<SeckillActivity>(`/seckill/activitiy/${id}`);
        this.currentActivity = response.data;
      } catch (err: any) {
        this.error = err.response?.data?.message || err.message || '获取活动详情失败';
        console.error(`获取活动 ${id} 失败:`, err);
        // 如果直接获取失败且列表存在，则回退到列表查找
        // (如果强制直接获取，则移除此逻辑)
        if (this.activities.length > 0) {
            const activity = this.activities.find(act => act.id === Number(id));
            if (activity) this.currentActivity = activity;
        }
      } finally {
        this.isLoading = false;
      }
    },

    async performSeckill(payload: { userId: number; productId: number; activityId: number }) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.post('/seckill/doSeckill', null, { // 第三个参数是 config
          params: { // 使用 params 将数据作为 URL search parameters 发送
            userId: payload.userId,
            productId: payload.productId,
            activityId: payload.activityId
          }
        });
        console.log('Seckill response:', response.data);
        return response.data; // 返回给组件处理
      } catch (err: any) {
        this.error = err.response?.data?.message || err.message || '秒杀失败';
        console.error('Seckill failed:', err);
        return err.response?.data || { code: 500, message: this.error };
      } finally {
        this.isLoading = false;
      }
    }
  }
})

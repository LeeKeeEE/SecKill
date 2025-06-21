import { defineStore } from 'pinia'
import apiClient from '@/api/axios'
import type { OrderInfo } from '@/types'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [] as OrderInfo[],
    isLoading: false,
    error: null as string | null
  }),
  actions: {
    async fetchOrders(userId: number) {
      this.isLoading = true
      this.error = null
      try {
        //{ code, msg, data }
        const response = await apiClient.get(`/order/user/${userId}`)
        if (response.data && response.data.code === 0) {
          this.orders = response.data.data
        } else {
          this.error = response.data.msg || '获取订单列表失败'
          this.orders = []
        }
      } catch (err: any) {
        this.error = err.response?.data?.msg || err.message || '获取订单列表时发生错误'
        this.orders = []
        console.error('Failed to fetch orders:', err)
      } finally {
        this.isLoading = false
      }
    }
  }
})

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock?: number;
  image?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface SeckillActivity {
  id: number;
  name: string;
  product: Product; // 嵌套 Product 类型
  seckillPrice: number;
  stockCount: number; // 这个是秒杀活动的总库存
  startTime: string;  // ISO 8601 格式的日期字符串，例如 "2025-05-20T10:00:00.000+00:00"
  endTime: string;
  status: number; // 0: 未开始, 1: 进行中, 2: 已结束
}

export interface User {
  id: number;
  username: string;
  phone?: string;
  // 其他用户信息，根据需要添加
}

export interface OrderInfo {
  id: number;
  user: User;
  product: Product;
  orderAmount: number;
  status: number; // 0: 未支付, 1: 已支付, 2: 已取消
  createTime: string;
  payTime?: string;
}

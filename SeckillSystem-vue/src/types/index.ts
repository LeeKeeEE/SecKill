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
// 对应后端的 ProductVo
export interface ProductVo {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  image: string;
}

// 对应后端的 SeckillActivityVo
export interface SeckillActivityVo {
  id: number;
  name: string;
  seckillPrice: number;
  stockCount: number;
  startTime: string; // 后端是 Date, JSON序列化后变为字符串
  endTime: string;
  status: number;
  productVo: ProductVo; // 嵌套 ProductVo
}
export interface UserVO {
  id: number;
  username: string;
  phone: string;
}
export interface ProductInfoVo {
  id: number;
  name: string;
  description: string;
  price: number;
  image: string; // 注意：字段名是 image
  createTime: string;
  updateTime: string;
}
export interface OrderInfo {
  id: number;
  userVO: UserVO;
  productInfoVo: ProductInfoVo;
  orderAmount: number;
  status: number;
  createTime: string;
  payTime: string | null;
}

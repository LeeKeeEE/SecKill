package com.seckill.lab.seckillsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_info")
@Data
public class OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal orderAmount;

    private Integer status; // 0: 未支付, 1: 已支付, 2: 已取消

    private Date createTime;

    private Date payTime;
}
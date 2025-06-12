package com.seckill.lab.seckillsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
@Entity
@Table(name = "seckill_activity")
@Data
public class SeckillActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal seckillPrice;

    private Integer stockCount;

    private Date startTime;

    private Date endTime;

    private Integer status; // 0: 未开始, 1: 进行中, 2: 已结束


}
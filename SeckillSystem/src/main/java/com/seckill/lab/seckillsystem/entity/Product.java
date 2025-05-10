package com.seckill.lab.seckillsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String image;

    private Integer status; // 0: 下架, 1: 正常

    private Date createTime;

    private Date updateTime;
}
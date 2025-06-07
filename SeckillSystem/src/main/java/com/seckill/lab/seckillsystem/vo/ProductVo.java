package com.seckill.lab.seckillsystem.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class ProductVo {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String image;

}

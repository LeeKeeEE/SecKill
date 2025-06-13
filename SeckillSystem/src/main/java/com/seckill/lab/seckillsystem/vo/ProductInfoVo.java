package com.seckill.lab.seckillsystem.vo;

import com.seckill.lab.seckillsystem.entity.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class ProductInfoVo {
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private String image;

    private Date createTime;

    private Date updateTime;

    public ProductInfoVo(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.image = product.getImage();
        this.createTime = product.getCreateTime();
        this.updateTime = product.getUpdateTime();
    }
}

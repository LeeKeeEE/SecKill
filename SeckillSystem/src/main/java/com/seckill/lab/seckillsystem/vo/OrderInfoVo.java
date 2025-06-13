package com.seckill.lab.seckillsystem.vo;

import com.seckill.lab.seckillsystem.entity.OrderInfo;
import com.seckill.lab.seckillsystem.entity.Product;
import com.seckill.lab.seckillsystem.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class OrderInfoVo {
    private Long id;

    private UserVo userVO;

    private ProductInfoVo productInfoVo;

    private BigDecimal orderAmount;

    private Integer status; // 0: 未支付, 1: 已支付, 2: 已取消

    private Date createTime;

    private Date payTime;

    public OrderInfoVo(OrderInfo orderInfo) {
        this.id = orderInfo.getId();
        this.userVO = new UserVo(orderInfo.getUser());
        this.productInfoVo = new ProductInfoVo(orderInfo.getProduct());
        this.orderAmount = orderInfo.getOrderAmount();
        this.status = orderInfo.getStatus();
        this.createTime = orderInfo.getCreateTime();
        this.payTime = orderInfo.getPayTime();

    }
}

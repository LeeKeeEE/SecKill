package com.seckill.lab.seckillsystem.controller;

import com.seckill.lab.seckillsystem.entity.OrderInfo;
import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.service.OrderService;
import com.seckill.lab.seckillsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService; // 确保UserService可用于OrderService

    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<OrderInfo>> getOrdersByUserId(@PathVariable Long userId) {
        logger.info("查询用户ID={}的订单列表", userId);
        List<OrderInfo> orders = orderService.getOrdersByUserId(userId);
        return Result.success(orders);
    }
}
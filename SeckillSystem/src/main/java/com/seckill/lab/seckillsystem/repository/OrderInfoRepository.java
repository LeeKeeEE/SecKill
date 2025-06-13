package com.seckill.lab.seckillsystem.repository;

import com.seckill.lab.seckillsystem.entity.OrderInfo;
import com.seckill.lab.seckillsystem.entity.Product;
import com.seckill.lab.seckillsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
    List<OrderInfo> findByUser(User user);
    List<OrderInfo> findByUserId(Long id);

    // 检查给定用户和商品的订单是否存在
    boolean existsByUserAndProduct(User user, Product product);
}
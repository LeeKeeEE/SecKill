package com.seckill.lab.seckillsystem.repository;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Long> {
    List<SeckillActivity> findByStatus(Integer status);
}
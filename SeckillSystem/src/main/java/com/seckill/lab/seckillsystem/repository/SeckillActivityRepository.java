package com.seckill.lab.seckillsystem.repository;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Long> {
    List<SeckillActivity> findByStatus(Integer status);
    List<SeckillActivity> findByStatusAndStartTimeBefore(Integer status, Date currentTime);
    List<SeckillActivity> findByStatusAndEndTimeBefore(Integer status, Date currentTime);

    // 新增一个方法，通过ID查找活动并应用悲观写锁
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa FROM SeckillActivity sa WHERE sa.id = :id")
    Optional<SeckillActivity> findAndLockById(@Param("id") Long id);
}
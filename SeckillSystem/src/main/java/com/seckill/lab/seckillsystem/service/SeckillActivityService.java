package com.seckill.lab.seckillsystem.service;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.repository.SeckillActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillActivityService {

    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    public List<SeckillActivity> findAllActivities() {
        return seckillActivityRepository.findAll();
    }

    // 根据ID查找活动
    public Optional<SeckillActivity> findActivityById(Long id) {
        return seckillActivityRepository.findById(id);
    }

    public SeckillActivity saveActivity(SeckillActivity activity) {
        return seckillActivityRepository.save(activity);
    }

    // 将秒杀商品库存预热到Redis
    public void preloadSeckillStock(Long activityId) {
        Optional<SeckillActivity> activityOpt = seckillActivityRepository.findById(activityId);
        if (activityOpt.isPresent()) {
            SeckillActivity activity = activityOpt.get();
            // 将库存存入Redis
            String stockKey = SECKILL_STOCK_KEY + activityId;
            redisTemplate.opsForValue().set(stockKey, activity.getStockCount());

            // 设置过期时间为活动结束时间
            if (activity.getEndTime() != null) {
                long ttl = activity.getEndTime().getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.expire(stockKey, ttl, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    // 检查库存并减库存
    public boolean reduceStock(Long activityId) {
        String stockKey = SECKILL_STOCK_KEY + activityId;
        Long stock = redisTemplate.opsForValue().decrement(stockKey);
        return stock != null && stock >= 0;
    }
}
package com.seckill.lab.seckillsystem.service;

import com.seckill.lab.seckillsystem.entity.OrderInfo;
import com.seckill.lab.seckillsystem.entity.Product;
import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.entity.User;
import com.seckill.lab.seckillsystem.repository.SeckillActivityRepository;
import com.seckill.lab.seckillsystem.repository.OrderInfoRepository;
import com.seckill.lab.seckillsystem.repository.UserRepository;
import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.result.ResultCode;
import com.seckill.lab.seckillsystem.vo.OrderInfoVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillActivityService {

    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private UserRepository userRepository;

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

    @Transactional
    public Result<?> performSeckillDbOnly(Long userId, Long productId, Long activityId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Result.error(ResultCode.MOBILE_NOT_EXIST);
        }

        // 1. 锁定活动行以处理并发
        Optional<SeckillActivity> activityOpt = seckillActivityRepository.findAndLockById(activityId);
        if (activityOpt.isEmpty()) {
            return Result.error(ResultCode.ACTIVITY_INVALID);
        }
        SeckillActivity activity = activityOpt.get();
        Product product = activity.getProduct();

        // 2. 检查产品是否匹配
        if (!product.getId().equals(productId)) {
            return Result.error(ResultCode.REQUEST_ILLEGAL.fillArgs("产品ID不匹配"));
        }

        // 3. 检查重复订单
        if (orderInfoRepository.existsByUserAndProduct(userOpt.get(), product)) {
            return Result.error(ResultCode.REPEAT_SECKILL);
        }

        // 4. 检查库存
        if (activity.getStockCount() <= 0) {
            return Result.error(ResultCode.SECKILL_OVER_ERROR);
        }

        // 5. 扣减库存
        activity.setStockCount(activity.getStockCount() - 1);
        seckillActivityRepository.save(activity);

        // 6. 创建订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUser(userOpt.get());
        orderInfo.setProduct(product);
        orderInfo.setOrderAmount(activity.getSeckillPrice());
        orderInfo.setStatus(0); // 状态：未支付
        orderInfo.setCreateTime(new Date());
        orderInfoRepository.save(orderInfo);

        OrderInfoVo orderInfoVo = new OrderInfoVo(orderInfo);

        return Result.success(orderInfoVo);
    }
}
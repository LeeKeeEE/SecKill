package com.seckill.lab.seckillsystem.controller;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.service.OrderService;
import com.seckill.lab.seckillsystem.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 限流前缀
    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    @GetMapping("/activities")
    public List<SeckillActivity> getActivities() {
        return seckillActivityService.findAllActivities();
    }

    @PostMapping("/doSeckill")
    public Map<String, Object> doSeckill(@RequestParam Long userId,
                                         @RequestParam Long productId,
                                         @RequestParam Long activityId) {
        Map<String, Object> result = new HashMap<>();

        // 用户请求限流控制
        String limitKey = RATE_LIMIT_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(limitKey);
        if (count == 1) {
            // 设置60秒过期
            redisTemplate.expire(limitKey, 60, TimeUnit.SECONDS);
        }

        // 限制用户60秒内最多请求10次
        if (count != null && count > 10) {
            result.put("code", 500);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }

        // 先检查并减Redis中的库存
        boolean hasStock = seckillActivityService.reduceStock(activityId);
        if (!hasStock) {
            result.put("code", 500);
            result.put("message", "库存不足");
            return result;
        }

        // 发送消息到队列
        boolean success = orderService.sendSeckillMessage(userId, productId, activityId);
        if (success) {
            result.put("code", 200);
            result.put("message", "秒杀请求已提交");
        } else {
            result.put("code", 500);
            result.put("message", "您已经参与过该商品的秒杀");
        }

        return result;
    }

    // 预热库存到Redis
    @PostMapping("/preload/{activityId}")
    public Map<String, Object> preloadSeckillStock(@PathVariable Long activityId) {
        Map<String, Object> result = new HashMap<>();
        seckillActivityService.preloadSeckillStock(activityId);
        result.put("code", 200);
        result.put("message", "库存预热成功");
        return result;
    }
}
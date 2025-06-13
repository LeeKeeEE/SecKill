package com.seckill.lab.seckillsystem.controller;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.repository.SeckillActivityRepository;
import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.result.ResultCode;
import com.seckill.lab.seckillsystem.service.OrderService;
import com.seckill.lab.seckillsystem.service.ProductService;
import com.seckill.lab.seckillsystem.service.SeckillActivityService;
import com.seckill.lab.seckillsystem.vo.SeckillActivityVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(SeckillController.class);

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

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

        Optional<SeckillActivity> activityOptional = seckillActivityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            result.put("code", 500);
            result.put("message", "活动不存在！");
            return result;
        }
        SeckillActivity activity = activityOptional.get();
        Date now = new Date();
        if (now.before(activity.getStartTime())) {
            logger.info("秒杀请求，活动未开始");
            result.put("code", 500);
            result.put("message", "活动尚未开始，请耐心等待！");
            return result;
        }
        if (now.after(activity.getEndTime())) {
            logger.info("秒杀请求，但活动已结束");
            result.put("code", 500);
            result.put("message", "抱歉，活动已经结束了。");
            return result;
        }

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

    // 根据活动id查看商品详情页
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getActivityDetails(@PathVariable Long activityId) {
        Optional<SeckillActivity> seckillActivity = seckillActivityService.findActivityById(activityId);
        if(seckillActivity.isPresent()) {
            SeckillActivity activity = seckillActivity.get();
            SeckillActivityVo seckillActivityVo = new SeckillActivityVo();
            BeanUtils.copyProperties(activity, seckillActivityVo);
            if (activity.getProduct() != null) {
                seckillActivityVo.setProductVo(activity.getProduct());
            }
            logger.info(seckillActivityVo.toString());
            return ResponseEntity.ok(seckillActivityVo);
        }else{
            Result<Boolean> errorResult = Result.error(ResultCode.ACTIVITY_INVALID.fillArgs("目标活动不存在"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResult);
        }
    }

    @PostMapping("/doSeckillDbOnly")
    public Result<?> doSeckillDbOnly(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam Long activityId) {
        // 对活动状态进行初步检查
        Optional<SeckillActivity> activityOptional = seckillActivityService.findActivityById(activityId);
        if (activityOptional.isEmpty()) {
            return Result.error(ResultCode.ACTIVITY_INVALID);
        }
        SeckillActivity activity = activityOptional.get();
        Date now = new Date();
        if (now.before(activity.getStartTime())) {
            return Result.error(ResultCode.SECKILL_FAIL.fillArgs("活动尚未开始"));
        }
        if (now.after(activity.getEndTime())) {
            return Result.error(ResultCode.SECKILL_FAIL.fillArgs("活动已经结束"));
        }


        
        // 将事务逻辑委托给服务层
        try {
            return seckillActivityService.performSeckillDbOnly(userId, productId, activityId);
        } catch (Exception e) {
            logger.error("纯数据库秒杀失败, activityId: {}, userId: {}", activityId, userId, e);
            return Result.error(ResultCode.SECKILL_FAIL.fillArgs(e.getMessage()));
        }
    }
}
package com.seckill.lab.seckillsystem.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取单个对象
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        String realKey = prefix.getPrefix() + key;
        Object obj = redisTemplate.opsForValue().get(realKey);
        logger.info("get realKey:{}, value:{}", realKey, obj);
        return obj == null ? null : clazz.cast(obj);
    }

    /**
     * 设置redis对象
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        String realKey = prefix.getPrefix() + key;
        int seconds = prefix.expireSeconds();
        logger.info("set realKey:{}, value:{}, expire:{}", realKey, value, seconds);

        if (seconds > 0) {
            redisTemplate.opsForValue().set(realKey, value, Duration.ofSeconds(seconds));
        } else {
            redisTemplate.opsForValue().set(realKey, value);
        }
        return true;
    }

    /**
     * 删除对象
     */
    public boolean delete(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return Boolean.TRUE.equals(redisTemplate.delete(realKey));
    }

    /**
     * 是否存在key
     */
    public boolean exitsKey(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(realKey));
    }

    /**
     * 自增
     */
    public Long incr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().increment(realKey);
    }

    /**
     * 自减
     */
    public Long decr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().decrement(realKey);
    }

    /**
     * 不带前缀的 set 方法
     */
    public <T> boolean set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
        return true;
    }

    /**
     * 不带前缀的 get 方法
     */
    public <T> T get(String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj == null ? null : clazz.cast(obj);
    }
}

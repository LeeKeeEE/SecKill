// KeyPrefix.java
package com.seckill.lab.seckillsystem.redis;

public interface KeyPrefix {
    int expireSeconds();  // 过期时间（秒），0表示不过期
    String getPrefix();   // 前缀
}

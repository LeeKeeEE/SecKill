package com.seckill.lab.seckillsystem.redis;

public abstract class BasePrefix implements KeyPrefix {
    private int expireSeconds;
    private String prefix;

    // 默认过期时间为0（不过期）
    public BasePrefix(String prefix) {
        this(0, prefix);
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        // 类名小写 + prefix
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}

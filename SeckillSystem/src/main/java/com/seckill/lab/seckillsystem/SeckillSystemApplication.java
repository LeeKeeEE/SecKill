package com.seckill.lab.seckillsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.seckill.lab.seckillsystem.dao")  // 加上这一行，指定Mapper接口扫描路径
public class SeckillSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillSystemApplication.class, args);
    }

}

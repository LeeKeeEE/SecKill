package com.seckill.lab.seckillsystem.init;

import com.seckill.lab.seckillsystem.entity.Product;
import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.entity.User;
import com.seckill.lab.seckillsystem.service.ProductService;
import com.seckill.lab.seckillsystem.service.SeckillActivityService;
import com.seckill.lab.seckillsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Override
    public void run(String... args) throws Exception {
        // 判断是否已有数据，避免重复初始化
        if (userService.findUserById(1L).isPresent()) {
            return;
        }

        // 初始化用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("e10adc3949ba59abbe56e057f20f883e"); // 123456的MD5
        user.setSalt("abc123");
        user.setPhone("13800138000");
        user.setRegisterDate(new Date());
        userService.saveUser(user);

        // 初始化商品
        Product product = new Product();
        product.setName("iPhone 15");
        product.setDescription("Apple最新旗舰手机");
        product.setPrice(new BigDecimal("6999.00"));
        product.setStock(100);
        product.setStatus(1);
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product = productService.saveProduct(product);

        // 初始化秒杀活动
        SeckillActivity activity = new SeckillActivity();
        activity.setName("iPhone 15限时秒杀");
        activity.setProduct(product);
        activity.setSeckillPrice(new BigDecimal("5999.00"));
        activity.setStockCount(10);

        // 活动时间：当前时间开始，持续2小时
        Date now = new Date();
        activity.setStartTime(now);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, 2);
        activity.setEndTime(calendar.getTime());

        activity.setStatus(1); // 进行中
        activity = seckillActivityService.saveActivity(activity);

        // 预热商品库存到Redis
        seckillActivityService.preloadSeckillStock(activity.getId());
    }
}
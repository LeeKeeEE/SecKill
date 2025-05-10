package com.seckill.lab.seckillsystem.mq;

import com.seckill.lab.seckillsystem.config.RabbitMQConfig;
import com.seckill.lab.seckillsystem.entity.OrderInfo;
import com.seckill.lab.seckillsystem.entity.Product;
import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.entity.User;
import com.seckill.lab.seckillsystem.service.OrderService;
import com.seckill.lab.seckillsystem.service.ProductService;
import com.seckill.lab.seckillsystem.service.SeckillActivityService;
import com.seckill.lab.seckillsystem.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
public class SeckillConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    @Transactional
    public void receiveSeckillMessage(Map<String, Object> message) {
        Long userId = (Long) message.get("userId");
        Long productId = (Long) message.get("productId");
        Long activityId = (Long) message.get("activityId");

        // 检查用户是否存在
        Optional<User> userOpt = userService.findUserById(userId);
        // 检查商品是否存在
        Optional<Product> productOpt = productService.findProductById(productId);
        // 检查活动是否存在
        Optional<SeckillActivity> activityOpt = seckillActivityService.findActivityById(activityId);

        if (userOpt.isPresent() && productOpt.isPresent() && activityOpt.isPresent()) {
            User user = userOpt.get();
            Product product = productOpt.get();
            SeckillActivity activity = activityOpt.get();

            // 检查活动是否在进行中
            Date now = new Date();
            if (activity.getStartTime().before(now) && activity.getEndTime().after(now)) {
                // 创建订单
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setUser(user);
                orderInfo.setProduct(product);
                orderInfo.setOrderAmount(activity.getSeckillPrice());
                orderInfo.setStatus(0); // 未支付
                orderInfo.setCreateTime(now);

                orderService.createOrder(orderInfo);

                // 数据库中减库存
                activity.setStockCount(activity.getStockCount() - 1);
                seckillActivityService.saveActivity(activity);

                // 实际项目中还需要处理其他业务逻辑，如通知用户等
            }
        }
    }
}
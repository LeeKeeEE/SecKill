package com.seckill.lab.seckillsystem.service;

import com.seckill.lab.seckillsystem.entity.SeckillActivity;
import com.seckill.lab.seckillsystem.repository.SeckillActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private SeckillActivityRepository activityRepository;

    /**
     * 每分钟执行一次，检查是否有活动需要开始
     * cron = "0 * * * * ?" 表示每分钟的第0秒触发
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndStartActivities() {
        log.info("定时任务：开始检查需要启动的秒杀活动...");
        // 查找所有状态为“未开始”且开始时间已到的活动
        List<SeckillActivity> activitiesToStart = activityRepository.findByStatusAndStartTimeBefore(0, new Date());

        for (SeckillActivity activity : activitiesToStart) {
            activity.setStatus(1); // 将状态更新为“进行中”
            activityRepository.save(activity);
            log.info("活动 '{}' (ID: {}) 已自动开始。", activity.getName(), activity.getId());
        }
    }

    /**
     * 每分钟执行一次，检查是否有活动需要结束
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndEndActivities() {
        log.info("定时任务：开始检查需要结束的秒杀活动...");
        // 查找所有状态为“进行中”且结束时间已到的活动
        List<SeckillActivity> activitiesToEnd = activityRepository.findByStatusAndEndTimeBefore(1, new Date());

        for (SeckillActivity activity : activitiesToEnd) {
            activity.setStatus(2); // 将状态更新为“已结束”
            activityRepository.save(activity);
            log.info("活动 '{}' (ID: {}) 已自动结束。", activity.getName(), activity.getId());
        }
    }
}
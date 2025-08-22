package com.qbot.cq.business.user.scheduleds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyScheduledTask {
    // 每5秒执行一次
    @Scheduled(fixedDelay = 5000)
    public void runTask() {
    }
}
package org.clf.springboot.schedule;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleManager {

    // TODO 定期删除过期的图片
    @Scheduled(cron = "0 0 0 * * 0")
    public void cleanExpiredImgs() {

    }
}

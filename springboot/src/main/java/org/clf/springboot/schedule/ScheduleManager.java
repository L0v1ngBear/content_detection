package org.clf.springboot.schedule;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleManager {

    @Scheduled(cron = "0 0 0 * * 0")
    public void cleanExpiredImgs() {

    }
}

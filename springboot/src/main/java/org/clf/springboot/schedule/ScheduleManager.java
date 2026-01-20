package org.clf.springboot.schedule;


import jakarta.annotation.Resource;
import org.clf.springboot.entity.PictureStat;
import org.clf.springboot.mapper.PictureStatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScheduleManager {

    private final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

    private static final int SHARED_COUNT = 16;

    private static final String REDIS_KEY_PREFIX = "stat_";
    private static final String STAT_TYPE_PICTURE_REVIEW = "picture_review_count";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PictureStatMapper pictureStatMapper;

    // TODO 定期删除过期的图片
    @Scheduled(cron = "${scheduled.delete.pic}")
    public void cleanExpiredImgs() {

    }

    @Scheduled(cron = "${scheduled.syn.pic}")
    public void synPic() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        List<PictureStat> statList = new ArrayList<>();
        for (int index = 0; index < SHARED_COUNT; index++) {
            try {
                String redisKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_PICTURE_REVIEW, currentMonth, index);
                Map<Object, Object> data = stringRedisTemplate.opsForHash().entries(redisKey);
                if (CollectionUtils.isEmpty(data)) {
                    continue;
                }

                data.forEach((k, v) -> {
                    PictureStat pictureStat = new PictureStat();
                    pictureStat.setUserId(Long.valueOf(k.toString()));
                    pictureStat.setStatMonth(currentMonth);
                    pictureStat.setStatValue(Long.valueOf(v.toString()));
                    statList.add(pictureStat);
                });
            } catch (Exception e) {
                logger.error("图片数量同步到数据库失败", e);
            }
        }
        if (!CollectionUtils.isEmpty(statList)) {
            try {
                pictureStatMapper.batchUpsert(statList);
            } catch (Exception e) {
                logger.error("图片数量同步到数据库失败", e);
            }
        }
    }
}

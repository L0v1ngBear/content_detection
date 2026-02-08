package org.clf.springboot.schedule;

import jakarta.annotation.Resource;
import org.clf.springboot.entity.PictureStatics;
import org.clf.springboot.mapper.PictureStatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScheduleManager {

    private final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

    private static final int SHARED_COUNT = 16;
    private static final String REDIS_KEY_PREFIX = "stat_";
    private static final String STAT_TYPE_PICTURE_REVIEW = "picture_review_count";
    private static final int IMAGE_ID_EXPIRE_DAYS = 7;

    // 高性能清理配置（可根据服务器性能调整）
    private static final int BATCH_SIZE = 1000; // 每批次处理的Key数量
    private static final int SCAN_COUNT = 100;  // Scan每次扫描的数量（平衡性能和效率）
    private static final String CLEAN_LOCK_KEY = "lock:clean:expired:imgs"; // 分布式锁Key
    private static final long LOCK_EXPIRE_TIME = 30; // 锁过期时间（分钟）

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PictureStatMapper pictureStatMapper;

    @Value("${minio.redisKey}")
    private String redisPrefix;

    /**
     * 高性能清理过期图片ID（异步+Scan+批量删除+分布式锁）
     * 解决大数据量下效率低、阻塞Redis的问题
     */
    @Async // 异步执行，避免阻塞其他定时任务
    @Scheduled(cron = "${scheduled.delete.pic}")
    public void cleanExpiredImgs() {
        long startTime = System.currentTimeMillis();
        logger.info("开始执行高性能过期图片清理任务");

        // 1. 分布式锁
        // 置1拿锁
        Boolean lockSuccess = stringRedisTemplate.opsForValue().setIfAbsent(
                CLEAN_LOCK_KEY, "1", LOCK_EXPIRE_TIME, java.util.concurrent.TimeUnit.MINUTES
        );
        if (Boolean.FALSE.equals(lockSuccess)) {
            logger.info("已有节点执行清理任务，本次跳过");
            return;
        }

        try {
            // 2. 计算过期时间戳：当前时间 - IMAGE_ID_EXPIRE_DAYS 天
            long expireTimestamp = System.currentTimeMillis() - IMAGE_ID_EXPIRE_DAYS * 24 * 3600 * 1000;

            // 3. 用Scan替代Keys：非阻塞式遍历所有用户图片列表Key
            Set<String> imageListKeys = new HashSet<>();
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                        .match(redisPrefix + "*") // 匹配用户图片列表Key前缀
                        .count(SCAN_COUNT)         // 每次扫描100条，非阻塞
                        .build());
                while (cursor.hasNext()) {
                    String key = new String(cursor.next());
                    imageListKeys.add(key);
                    // 分批处理：每攒够BATCH_SIZE条先处理，避免内存溢出
                    if (imageListKeys.size() >= BATCH_SIZE) {
                        processImageListKeys(imageListKeys, expireTimestamp);
                        imageListKeys.clear();
                    }
                }
                cursor.close();
                return null;
            });

            // 处理剩余的Key（不足BATCH_SIZE的部分）
            if (!imageListKeys.isEmpty()) {
                processImageListKeys(imageListKeys, expireTimestamp);
            }

            long costTime = System.currentTimeMillis() - startTime;
            logger.info("高性能过期图片清理任务完成，耗时{}ms，总计处理{}个用户Key", costTime, imageListKeys.size());
        } catch (Exception e) {
            logger.error("定时任务：清理过期图片ID失败", e);
        } finally {
            // 释放分布式锁
            stringRedisTemplate.delete(CLEAN_LOCK_KEY);
            logger.info("释放清理任务分布式锁");
        }
    }

    /**
     * 批量处理用户图片列表Key（核心批量逻辑）
     * @param imageListKeys 待处理的用户Key列表
     * @param expireTimestamp 过期时间戳
     */
    private void processImageListKeys(Set<String> imageListKeys, long expireTimestamp) {
        // 1. 批量收集需要删除的详情Key和空ZSet Key
        List<String> needDeleteDetailKeys = new ArrayList<>();
        List<String> needDeleteEmptyZSetKeys = new ArrayList<>();

        // 2. 遍历处理每个用户Key
        for (String imageListKey : imageListKeys) {
            // 2.1 获取该Key下所有过期的图片ID
            Set<String> expiredImageIds = stringRedisTemplate.opsForZSet()
                    .rangeByScore(imageListKey, 0, expireTimestamp);
            if (CollectionUtils.isEmpty(expiredImageIds)) {
                continue;
            }

            // 2.2 批量删除ZSet中的过期元素（单命令高效删除）
            long deleteCount = stringRedisTemplate.opsForZSet()
                    .removeRangeByScore(imageListKey, 0, expireTimestamp);
            logger.info("清理Key[{}]下过期图片ID，数量：{}", imageListKey, deleteCount);

            // 2.3 收集需要删除的图片详情Key（批量删除用）
            String userId = imageListKey.replace(redisPrefix, "");
            List<String> detailKeys = expiredImageIds.stream()
                    .map(imageId -> redisPrefix + userId + imageId)
                    .collect(Collectors.toList());
            needDeleteDetailKeys.addAll(detailKeys);

            // 2.4 检查ZSet是否为空，收集空Key
            Long remainingCount = stringRedisTemplate.opsForZSet().size(imageListKey);
            if (remainingCount == null || remainingCount == 0) {
                needDeleteEmptyZSetKeys.add(imageListKey);
            }
        }

        // 3. 批量删除图片详情Key（1次命令删多个，减少网络IO）
        if (!needDeleteDetailKeys.isEmpty()) {
            stringRedisTemplate.delete(needDeleteDetailKeys);
            logger.info("批量删除过期图片详情Key，数量：{}", needDeleteDetailKeys.size());
        }

        // 4. 批量删除空ZSet Key
        if (!needDeleteEmptyZSetKeys.isEmpty()) {
            stringRedisTemplate.delete(needDeleteEmptyZSetKeys);
            logger.info("批量删除空ZSet Key，数量：{}", needDeleteEmptyZSetKeys.size());
        }
    }

    /*
        用于统计用户月度使用数量（保留原有逻辑，无改动）
     */
    @Scheduled(cron = "${scheduled.syn.pic}")
    public void synPic() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        List<PictureStatics> statList = new ArrayList<>();
        for (int index = 0; index < SHARED_COUNT; index++) {
            try {
                String redisKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_PICTURE_REVIEW, currentMonth, index);
                Map<Object, Object> data = stringRedisTemplate.opsForHash().entries(redisKey);
                if (CollectionUtils.isEmpty(data)) {
                    continue;
                }

                data.forEach((k, v) -> {
                    PictureStatics pictureStatics = new PictureStatics();
                    pictureStatics.setUserId(Long.valueOf(k.toString()));
                    pictureStatics.setStatMonth(currentMonth);
                    pictureStatics.setStatValue(Long.valueOf(v.toString()));
                    statList.add(pictureStatics);
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
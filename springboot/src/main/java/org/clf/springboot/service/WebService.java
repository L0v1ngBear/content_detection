package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebService {

    @Value("${minio.redisKey}")
    private String redisKey;

    @Resource
    private RedisTemplate redisTemplate;

    public void getHistoryPicture(String userId) {
        redisTemplate.opsForList().leftPush(redisKey, userId);
    }
}

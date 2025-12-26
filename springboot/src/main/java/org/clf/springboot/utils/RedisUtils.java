package org.clf.springboot.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Long getId(String key) {
        Long id = stringRedisTemplate.opsForValue().increment(key, 1);
        if (id == null) {
            stringRedisTemplate.opsForValue().set(key, "1");
            id = 1L;
        }
        return id;
    }


}

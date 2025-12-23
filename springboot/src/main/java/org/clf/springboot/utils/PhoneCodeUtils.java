package org.clf.springboot.utils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.clf.springboot.exception.CustomException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PhoneCodeUtils {

    private static final long SMS_CODE_EXPIRE_SECONDS = 60L;
    private static final String REDIS_PREFIX_KEY = "SMS_CODE:";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public boolean validate(String phone, String verifyCode) {
        String redisKey = REDIS_PREFIX_KEY + phone;
        String realCode = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.equals(verifyCode, realCode)) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    public String sendSmsCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new CustomException("手机号无效");
        }
        String redisKey = REDIS_PREFIX_KEY + phone;
        String code = RandomStringUtils.randomNumeric(6);
        redisTemplate.opsForHash().put(redisKey, "code", code);
        redisTemplate.expire(redisKey, SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        // TODO 向用户发送验证码
        return code;
    }
}

package org.clf.springboot.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.clf.springboot.common.Constants;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.entity.User;
import org.clf.springboot.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token 工具类
 * 包含：创建Token、解析Token、验证Token有效性、获取Token中的数据等功能
 */
@Component
public class TokenUtils {

    private final String REFRESH_TOKEN = "refresh_token:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.sign}")
    private String sign;

    @Value("${jwt.access-token.expire}")
    private int accessTokenExpireSeconds;

    @Value("${jwt.refresh-token.expire}")
    private int refreshTokenExpireSeconds;

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpireSeconds);
    }

    public String createRefreshToken(Long userId) {
        String refreshToken = createToken(userId, refreshTokenExpireSeconds);
        stringRedisTemplate.opsForValue().set(REFRESH_TOKEN + refreshToken, String.valueOf(userId), refreshTokenExpireSeconds, TimeUnit.SECONDS);
        return refreshToken;
    }

    /**
     * 创建JWT Token
     * @param userId  需要存储到Token中的数据
     * @return 生成的Token字符串
     */
    private String createToken(Long userId, Integer expireSeconds) {
        if (userId == null) {
            throw new IllegalArgumentException("token参数不能为空");
        }
        return JWT.create()
                .withAudience(String.valueOf(userId))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expireSeconds * 1000))
                .sign(Algorithm.HMAC256(sign));

    }

    public List<String> refreshAccessToken(String refreshToken) {
        String userId = stringRedisTemplate.opsForValue().get(REFRESH_TOKEN + refreshToken);
        if (userId == null) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        List<String> back = new ArrayList<>();
        back.add(createAccessToken(Long.valueOf(userId)));
        stringRedisTemplate.delete(REFRESH_TOKEN + refreshToken);
        back.add(createRefreshToken(Long.valueOf(userId)));
        return back;
    }

    public Boolean logout(String refreshToken) {
        return stringRedisTemplate.delete(REFRESH_TOKEN + refreshToken);
    }
}
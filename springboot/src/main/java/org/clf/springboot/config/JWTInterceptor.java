package org.clf.springboot.config;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.clf.springboot.common.Constants;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.entity.Account;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.AdminMapper;
import org.clf.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Value("${jwt.sign}")
    private String sign;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从http请求标头里面拿到token
        String accessToken = request.getHeader(Constants.TOKEN);
        if (ObjectUtil.isNull(accessToken)) {
            // 如果没拿到，那么再从请求参数里拿一次
            request.getParameter(Constants.TOKEN);
        }
        // 2. 开始执行认证
        if (ObjectUtil.isNull(accessToken) || !accessToken.startsWith("Bearer ")) {
            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
        }
        Account account = new Account();
        accessToken = accessToken.replace(Constants.TOKEN_PREFIX, "");
        try {
            String userId =JWT.decode(accessToken).getAudience().getFirst();
            account = userMapper.selectById(Long.valueOf(userId));
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }

        if (ObjectUtil.isNull(account)) {
            // 用户不存在
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(sign)).acceptExpiresAt(0).build();
            jwtVerifier.verify(accessToken);  // 验证token
        } catch (JWTVerificationException e) {
            // 用户不存在
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        request.setAttribute("userId", account.getId());
        return true;
    }
}
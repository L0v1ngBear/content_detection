package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.dto.LoginRequestDTO;
import org.clf.springboot.dto.LoginResponseDTO;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.loginStrategy.LoginStrategy;
import org.clf.springboot.loginStrategy.LoginStrategyFactory;
import org.clf.springboot.mapper.UserMapper;
import org.clf.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Resource
    private LoginStrategyFactory loginStrategyFactory;

    @Resource
    private TokenUtils tokenUtils;

    @Resource
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        String loginType = loginRequestDTO.getLoginType();
        LoginStrategy loginStrategy = loginStrategyFactory.getStrategy(loginType);
        Long userId = loginStrategy.login(loginRequestDTO);

        String accessToken = tokenUtils.createAccessToken(userId);
        String refreshToken = tokenUtils.createRefreshToken(userId);
        try {
            userMapper.selectById(userId);
        } catch (Exception e) {
            throw new CustomException("400", e.getMessage());
        }
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        loginResponseDTO.setRefreshToken(refreshToken);
        loginResponseDTO.setExpireTime(7200L);

        return loginResponseDTO;
    }

    public void logout(String refreshToken) {
        if (!tokenUtils.logout(refreshToken)) {
            throw new CustomException("500", "系统异常");
        }
    }

    public void sendSmsCode(String phone) {
        // TODO 接入短信服务商
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        List<String> res = tokenUtils.refreshAccessToken(refreshToken);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();

        loginResponseDTO.setAccessToken(res.getFirst());
        loginResponseDTO.setRefreshToken(res.getLast());
        loginResponseDTO.setExpireTime(7200L);

        return loginResponseDTO;
    }
}

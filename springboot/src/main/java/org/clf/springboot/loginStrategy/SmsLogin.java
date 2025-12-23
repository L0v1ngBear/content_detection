package org.clf.springboot.loginStrategy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.clf.springboot.dto.LoginRequestDTO;
import org.clf.springboot.entity.User;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.UserMapper;
import org.clf.springboot.utils.PhoneCodeUtils;
import org.springframework.stereotype.Component;

@Component
public class SmsLogin implements LoginStrategy {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PhoneCodeUtils phoneCodeUtils;

    @Override
    public Long login(LoginRequestDTO loginRequestDTO) {
        String phone = loginRequestDTO.getPhone();
        String verifyCode = loginRequestDTO.getVerifyCode();
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(verifyCode)) {
            throw new CustomException("手机号或验证码不能为空");
        }
        if (!phoneCodeUtils.validate(phone, verifyCode)) {
            throw new CustomException("验证码过期或错误");
        }
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setUsername("SMS_" + phone);
            user.setPassword("123456");
            userMapper.insert(user);
        }
        return user.getId();
    }

    @Override
    public String getSupportType() {
        return "SMS_CODE";
    }


}

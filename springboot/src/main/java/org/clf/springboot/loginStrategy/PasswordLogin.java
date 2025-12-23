package org.clf.springboot.loginStrategy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.clf.springboot.dto.LoginRequestDTO;
import org.clf.springboot.entity.User;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordLogin implements LoginStrategy {

    @Resource
    private UserMapper userMapper;

    @Override
    public Long login(LoginRequestDTO loginRequestDTO) {
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new CustomException("账号或密码不能为空");
        }
        User userList = userMapper.selectByUsername(username);
        if (userList == null) {
            throw new CustomException("用户不存在");
        }
        boolean passWordMatch = BCrypt.checkpw(password, userList.getPassword());
        if (!passWordMatch) {
            throw new CustomException("密码错误");
        }
        return userList.getId();
    }

    @Override
    public String getSupportType() {
        return "PASSWORD";
    }
}

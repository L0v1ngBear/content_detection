package org.clf.springboot.loginStrategy;

import org.clf.springboot.dto.LoginRequestDTO;
import org.clf.springboot.dto.LoginResponseDTO;

public interface LoginStrategy {

    /**
     *执行登录逻辑，获取用户id
     */
    Long login(LoginRequestDTO loginRequestDTODTO);

    /**
     *获取支持的登录类型
     */
    String getSupportType();
}

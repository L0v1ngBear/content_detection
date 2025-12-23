package org.clf.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    /** 登录类型：PASSWORD/SMS_CODE/WECHAT/ALIPAY */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
    /** 账号（仅密码登录） */
    private String username;
    /** 密码（仅密码登录，前端加密后传递） */
    private String password;
    /** 手机号（仅短信登录） */
    private String phone;
    /** 验证码（仅短信登录） */
    private String verifyCode;
    /** 第三方授权码（仅微信/支付宝登录） */
    private String authCode;
}

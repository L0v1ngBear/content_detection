package org.clf.springboot.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String accessToken; // JWT Access Token
    private String refreshToken; // 刷新Token（Redis存储）
    private Long expireTime; // Access Token过期时间（秒）
}
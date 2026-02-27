package org.clf.springboot.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String avatar;
}

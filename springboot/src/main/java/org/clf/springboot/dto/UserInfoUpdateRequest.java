package org.clf.springboot.dto;

import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String avatar;
}

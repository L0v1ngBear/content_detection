package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {

    @TableField(exist = false)
    private Long Id;

    private String username;

    private String password;

    @TableField(exist = false)
    private String newPassword;

    @TableField(exist = false)
    private String token;

    private String role;
}

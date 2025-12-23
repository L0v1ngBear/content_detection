package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName("user")
@Getter
@Setter
public class User extends Account{

    @TableId(type = IdType.AUTO) // 自增策略（适配MySQL，等价于JPA的GenerationType.IDENTITY）
    private Long Id;

    // MP字段注解（替代JPA的@Column，属性名和字段名一致时可省略，此处保留显式配置）
    @TableField(value = "username", insertStrategy = FieldStrategy.NOT_NULL) // 插入时非空（等价于nullable = false）
    private String username;

    @TableField(value = "password", insertStrategy = FieldStrategy.NOT_NULL)
    private String password;

    @TableField("nickname") // 字段名和属性名一致，可仅指定value（或省略该注解）
    private String nickname;

    @TableField(
            value = "status",
            insertStrategy = FieldStrategy.NOT_NULL,
            fill = FieldFill.INSERT
    )
    private Integer status = 1;

    @TableField(value = "create_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    // 插入+更新时自动填充更新时间
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("avatar")
    private String avatar;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

}
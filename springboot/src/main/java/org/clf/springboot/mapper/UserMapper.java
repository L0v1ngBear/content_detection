package org.clf.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.clf.springboot.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE username = #{username} and status = '1' limit 1")
    User selectByUsername(String username);

    @Select("SELECT `phone`, `Id` FROM `user` WHERE phone = #{phone} and status = '1' limit 1")
    User selectByPhone(String phone);
}

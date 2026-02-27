package org.clf.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.clf.springboot.entity.ApiKey;

import java.util.List;

public interface ApiKeyMapper extends BaseMapper<ApiKey> {

    @Select("select count(1) from api_key where access_key = #{accessKey}")
    boolean existsByAccessKey(String accessKey);

    @Select("select * from api_key where user_id = #{userId}")
    List<ApiKey> selectByUserId(Long userId);
}

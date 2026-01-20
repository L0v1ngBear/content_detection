package org.clf.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Insert;
import org.clf.springboot.entity.PictureStat;

import java.util.List;

public interface PictureStatMapper extends BaseMapper<PictureStat> {

    @Insert({
            "<script>",
            "INSERT INTO user_stat (user_id, stat_month, stat_value, update_time)",
            "VALUES",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.userId}, #{item.statMonth}, #{item.statValue}, NOW())",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE",
            "stat_value = stat_value,",
            "update_time = NOW()",
            "</script>"
    })
    int batchUpsert(@Param("list") List<PictureStat> list);
}

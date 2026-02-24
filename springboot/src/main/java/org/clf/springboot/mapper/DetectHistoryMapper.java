package org.clf.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.clf.springboot.entity.DetectHistory;

public interface DetectHistoryMapper extends BaseMapper<DetectHistory> {

    @Select("SELECT * FROM detect_history WHERE object_id = #{imageId} and user_id = #{userId}")
    DetectHistory selectByImageId(String imageId, Long userId);

    @Select("SELECT COUNT(1) FROM detect_history WHERE object_id = #{imageId}")
    int existsByImageId(String imageId);

    @Update("update picture set status = 'ERROR' where object_id = #{imageId}")
    void updateErrorStatusByImageId(String imageId);

    @Update("update detect_history set status = #{status} , detect_time = #{detectTime}, violation_type = #{violationType} , confidence = #{confidence} where object_id = #{objectId}")
    void updateStatusById(DetectHistory detectHistory);

    @Update("update detect_history set status = #{status} where object_id = #{objectId}")
    void updateErrorStatus(DetectHistory detectHistory);
}

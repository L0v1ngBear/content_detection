package org.clf.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.clf.springboot.entity.Msg;

import java.util.List;

public interface MsgMapper extends BaseMapper<Msg> {

    @Update("update msg set is_read = 1 where id = #{id}")
    void setMsgReadById(Integer id);
}

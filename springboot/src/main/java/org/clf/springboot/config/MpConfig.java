package org.clf.springboot.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MpConfig implements MetaObjectHandler {

    // 插入操作时填充字段
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "status", Integer.class, 1);
        strictInsertFill(metaObject, "nickName", String.class, String.valueOf(metaObject.getValue("username")));
    }

    // 更新操作时填充字段
    @Override
    public void updateFill(MetaObject metaObject) {
        // 仅填充updateTime
        System.out.println(LocalDateTime.now());
        setFieldValByName("updateTime",LocalDateTime.now(), metaObject);
    }
}

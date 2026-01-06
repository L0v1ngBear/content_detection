package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类（对应msg表）
 */
@Data // Lombok：自动生成get/set/toString/equals等方法
@TableName("msg") // MyBatis-Plus：指定对应数据库表名
public class Msg {
    /**
     * 消息唯一ID
     */
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    /**
     * 关联用户ID
     */
    private String userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（system/detect/warning/error）
     */
    private String type;

    /**
     * 是否已读（0=未读，1=已读）
     */
    private Integer isRead;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
}
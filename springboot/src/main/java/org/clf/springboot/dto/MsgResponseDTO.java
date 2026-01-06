package org.clf.springboot.dto;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息返回前端DTO
 */
@Data
public class MsgResponseDTO {
    /**
     * 消息唯一ID
     */
    private Long id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（system/detect/warning/error）
     */
    private String type;

    /**
     * 是否已读（转换为布尔值，更贴合前端Vue的使用习惯）
     */
    private Boolean isRead;

    /**
     * 消息创建时间（格式化后字符串）
     */
    private LocalDateTime createTime;
}
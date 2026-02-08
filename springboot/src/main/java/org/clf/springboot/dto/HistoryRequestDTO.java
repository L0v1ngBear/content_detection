package org.clf.springboot.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class HistoryRequestDTO {

    Long userId;

    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 页大小（默认10，最大100）
     */
    private Integer pageSize = 10;

    /**
     * 开始时间（检测开始时间范围）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间（检测开始时间范围）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 检测状态（0-待检测，1-检测中，2-检测成功，3-检测失败）
     */
    private Integer status;

    /**
     * 检测类型（picture/video）
     */
    private String detectType;
}



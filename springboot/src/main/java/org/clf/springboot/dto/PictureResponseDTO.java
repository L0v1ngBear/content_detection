package org.clf.springboot.dto;

import lombok.Data;

@Data
public class PictureResponseDTO {

    private String taskId;

    /**
     * 检测状态（0-待检测，1-检测中，2-检测成功，3-检测失败）
     */
    private Integer status;

    private String violationType;

    private Double confidence;
}

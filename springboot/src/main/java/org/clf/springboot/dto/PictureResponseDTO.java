package org.clf.springboot.dto;

import java.util.List;

public class PictureResponseDTO {

    private String taskId;
    // 是否通过检测
    private boolean isPass;
    // 违规类型（如：涉黄、涉政、广告等）
    private String violationType;
    // 违规置信度（0-100）
    private int violationScore;
    // 违规区域坐标（[{x1,y1,x2,y2}, ...]）
    private List<int[]> violationArea;
    // 检测状态：completed/failed
    private String status;
    // 错误信息（失败时）
    private String msg;
}

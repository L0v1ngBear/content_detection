package org.clf.springboot.dto;

import lombok.Data;

@Data
public class PictureReviewDTO {
    private String imageId;
    private Long userId;
    private String objectName;
    private String preSignedUrl;
    private String requestId;
    private Long uploadTime;
    private String status;
    private Double yoloScore; // AI审核分数

}

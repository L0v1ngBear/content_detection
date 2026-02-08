package org.clf.springboot.dto;

import lombok.Data;

@Data
public class PictureReviewDTO extends BaseReviewResponseDTO {
    private String imageId;
    private Long userId;
    private String objectName;
    private String preSignedUrl;
    private String detectType = "img";
}

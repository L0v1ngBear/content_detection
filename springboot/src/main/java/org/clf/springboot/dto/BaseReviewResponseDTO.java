package org.clf.springboot.dto;

import lombok.Data;

@Data
public class BaseReviewResponseDTO {

    private Long id;

    private String fileName;

    private String status;

    private Long uploadTime;

    private Long detectStartTime;

    private String detectType;
}

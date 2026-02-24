package org.clf.springboot.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DetectHistoryResponseDTO {

    private Date detectTime;

    private String detectType;

    private String fileName;

    private Integer status;

    private String violationType;

    private String presignedUrl;
}

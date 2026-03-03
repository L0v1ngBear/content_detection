package org.clf.springboot.dto;

import lombok.Data;

@Data
public class VideoResultDTO {

    private String taskId;

    private String status;

    private String violationType;

    private Double confidence;
}

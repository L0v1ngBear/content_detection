package org.clf.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DetectHistory {

    private Long id;

    private Long userId;

    private String objectName;

    private Date detectTime;

    private String detectType;

    private Integer status;

    private String presignedUrl;

    private String violationType;

    private String createTime;

    private String updateTime;

    private String objectId;

    private Double confidence;

    private String fileName;
}

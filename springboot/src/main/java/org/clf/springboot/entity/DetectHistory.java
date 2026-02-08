package org.clf.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DetectHistory {

    private Long id;

    private String name;

    private Date detectTime;

    private String detectType;

    private String status;

    private String signedUrl;
}

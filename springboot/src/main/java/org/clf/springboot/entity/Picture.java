package org.clf.springboot.entity;

import lombok.Data;

@Data
public class Picture {

    private Long id;
    private String imageId;
    private String ObjectName;
    private String preSignedUrl;
    private String status;
    private Long updateTime;
    private Double yoloScore;
}

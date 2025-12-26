package org.clf.springboot.entity;

import lombok.Data;

@Data
public class Picture {

    private String id;
    private String imageId;
    private String ObjectName;
    private String preSignedUrl;
    private String status;
}

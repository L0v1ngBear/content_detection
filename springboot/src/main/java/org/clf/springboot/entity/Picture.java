package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("picture")
@Data
public class Picture {

    private Long id;
    private Long userId;
    private String imageId;
    private String objectName;
    private String preSignedUrl;
    private String status;
    private Long uploadTime;
    private Double yoloScore;
}

package org.clf.springboot.dto;


import cn.hutool.core.date.DateTime;
import lombok.Data;
import javax.persistence.MappedSuperclass;

@Data
public class BaseReviewResponseDTO {


    private String fileName;

    private String status;

    private DateTime uploadTime;

    private Long detectStartTime;

    private String detectType;
}

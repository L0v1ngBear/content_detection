package org.clf.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PictureStat {
    private Long id;
    private Long userId;
    private String statMonth;
    private Long statValue;
    private Date updateTime;
}

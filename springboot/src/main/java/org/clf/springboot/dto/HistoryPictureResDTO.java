package org.clf.springboot.dto;

import lombok.Data;
import org.clf.springboot.entity.Picture;

@Data
public class HistoryPictureResDTO {
    private String total;
    private Picture picture;
}

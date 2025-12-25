package org.clf.springboot.dto;

import lombok.Data;
import org.clf.springboot.entity.Picture;

import java.util.List;

@Data
public class HistoryPictureResDTO {
    private Integer total;
    private List<Picture> picture;
}

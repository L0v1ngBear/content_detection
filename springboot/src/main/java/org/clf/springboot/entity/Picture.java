package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@TableName("picture")
@Data
public class Picture {

    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String imageId;

    @NotBlank
    private String objectName;

    @NotBlank
    private String status;

    private Long uploadTime;

    private Double yoloScore;

    public Picture() {

    }
}

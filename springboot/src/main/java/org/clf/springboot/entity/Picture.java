package org.clf.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.clf.springboot.dto.BaseReviewResponseDTO;
import org.jetbrains.annotations.NotNull;

@TableName("picture")
@Data
public class Picture extends BaseReviewResponseDTO {

    @NotNull
    private Long userId;

    @NotBlank
    private String imageId;

    @NotBlank
    private String objectName;

    private Double yoloScore;

    private String detectType = "img";

    public Picture() {

    }

}

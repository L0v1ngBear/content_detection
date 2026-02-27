package org.clf.springboot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成API Key的请求参数
 */
@Data
public class ApiKeyGenerateRequest {

    /**
     * 关联的用户ID（必填）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * Key名称（可选）
     */
    private String keyName = "";

    /**
     * 有效期天数（0表示永久，默认30天）
     */
    @Min(value = 0, message = "有效期天数不能小于0")
    private Integer expireDays = 30;
}
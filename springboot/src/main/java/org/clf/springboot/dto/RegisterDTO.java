package org.clf.springboot.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册专属DTO
 * 继承基础DTO，补充注册专属字段
 */
@Data
@Schema(description = "注册请求参数")
public class RegisterDTO{

    private String username;

    private String phone;

    @Schema(description = "密码（6-16位，含字母+数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9]{6,16}$", message = "密码需为6-16位，且包含字母和数字")
    private String password;

    @Schema(description = "确认密码（和密码一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private String verifyCode;
}
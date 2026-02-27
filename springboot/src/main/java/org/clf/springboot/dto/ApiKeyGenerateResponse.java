package org.clf.springboot.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 生成API Key的响应结果
 */
@Data
public class ApiKeyGenerateResponse {
    /**
     * 响应数据体
     */
    @Data
    public static class DataDTO {
        private Long id;
        private Long userId;
        private String accessKey; // 仅生成时返回完整Key
        private String secretKey;
        private String keyName;
        private String status;
        private LocalDateTime expireTime;
        private LocalDateTime createTime;
        private String tips; // 提示信息
    }
}
package org.clf.springboot.entity;

import lombok.Data;
import org.springframework.retry.annotation.CircuitBreaker;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * API Key实体类（和Python端校验的表结构完全一致）
 */
@Data
@Entity
@Table(name = "api_keys") // 表名和Python端保持一致
public class ApiKey {

    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户ID（根据你的业务调整）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * API Key（唯一），格式：sk_ + 24位随机字符串
     */
    @Column(name = "access_key", nullable = false, unique = true, length = 64)
    private String accessKey;

    @Column(name = "secret_key", nullable = false, length = 128)
    private String secretKey;

    /**
     * Key名称（可选）
     */
    @Column(name = "key_name", length = 100)
    private String keyName;

    /**
     * 状态：active-激活，disabled-禁用，expired-过期
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private KeyStatus status = KeyStatus.active;

    /**
     * 过期时间（NULL表示永久）
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    /**
     * Key状态枚举
     */
    public enum KeyStatus {
        active, disabled, expired
    }
}
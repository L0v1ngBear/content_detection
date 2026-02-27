package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.entity.ApiKey;
import org.clf.springboot.mapper.ApiKeyMapper;
import org.clf.springboot.utils.ApiKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


/**
 * API Key生成服务
 */
@Service
public class ApiKeyService {

    @Resource
    private ApiKeyMapper apiKeyMapper;

    /**
     * 生成唯一的API Key
     */
    private String generateUniqueAccessKey() {
        SecureRandom random = new SecureRandom();
        String prefix = "sk_";
        while (true) {
            // 生成24位随机字符串（和Python端格式一致）
            String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            String accessKey = prefix + randomStr;
            // 检查是否重复（避免冲突）
            if (!apiKeyMapper.existsByAccessKey(accessKey)) {
                return accessKey;
            }
        }
    }

    /**
     * 生成API Key并存储到数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiKey generateApiKey(Long userId, String keyName, Integer expireDays) {
        // 1. 生成唯一Key
        String accessKey = ApiKeyGenerator.generateAccessKey();
        String rawSecretKey = ApiKeyGenerator.generateRandomString(32); // 原始值（返回给前端）
        String encryptedSecretKey = ApiKeyGenerator.generateSecretKey(rawSecretKey); // 加密后的值（存储）

        // 2. 计算过期时间
        LocalDateTime expireTime = null;
        if (expireDays > 0) {
            expireTime = LocalDateTime.now().plus(expireDays, ChronoUnit.DAYS);
        }

        // 3. 构建实体
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setAccessKey(accessKey);
        apiKey.setKeyName(keyName);
        apiKey.setStatus(ApiKey.KeyStatus.active);
        apiKey.setExpireTime(expireTime);
        apiKey.setSecretKey(encryptedSecretKey);
        apiKey.setCreateTime(LocalDateTime.now());
        apiKey.setUpdateTime(LocalDateTime.now());

        // 4. 保存到数据库
        apiKeyMapper.insert(apiKey);

        apiKey.setSecretKey(rawSecretKey);

        return apiKey;
    }

    public List<ApiKey> getApiKeyList(Long userId) {

        return apiKeyMapper.selectByUserId(userId);
    }

    public void toggleApiKeyStatus(Long keyId) {

        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) {
            throw new IllegalArgumentException("API Key不存在");
        }
        ApiKey.KeyStatus currentStatus = apiKey.getStatus();
        if (currentStatus == ApiKey.KeyStatus.active) {
            apiKey.setStatus(ApiKey.KeyStatus.disabled);
        } else {
            apiKey.setStatus(ApiKey.KeyStatus.active);
        }
        apiKeyMapper.updateById(apiKey);
    }

    public void resetApiKey(Long keyId, String userId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) {
            throw new IllegalArgumentException("API Key不存在");
        }
        if (!apiKey.getUserId().equals(Long.parseLong(userId))) {
            throw new IllegalArgumentException("用户权限不足");
        }
        // 生成新的Key
        String newSecretKey = ApiKeyGenerator.generateRandomString(32);
        apiKey.setSecretKey(ApiKeyGenerator.generateSecretKey(newSecretKey));
        apiKeyMapper.updateById(apiKey);
        // 返回新的Key
        //TODO 将加密后的数据发送到用户邮箱

    }

    public void deleteApiKey(Long keyId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) {
            throw new IllegalArgumentException("API Key不存在");
        }
        apiKeyMapper.deleteById(apiKey.getId());
    }
}
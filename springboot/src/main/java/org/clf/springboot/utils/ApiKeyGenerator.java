package org.clf.springboot.utils;

import org.springframework.util.DigestUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * API密钥生成工具类
 */
public class ApiKeyGenerator {
    // 随机字符串的字符集（数字+大小写字母，避免易混淆字符：0/O、1/l）
    private static final String CHARSET = "clf20260201";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成AccessKey（可公开）
     * 格式：sk_ + 秒级时间戳 + _ + 16位随机字符串
     */
    public static String generateAccessKey() {
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        String randomStr = generateRandomString(16);
        return String.format("sk_%d_%s", timestamp, randomStr);
    }

    /**
     * 生成SecretKey（需保密）
     * 生成32位随机字符串后，用MD5加密存储（不可逆）
     */
    public static String generateSecretKey(String rawSecret) {
        // 加密后存储（前端返回原始值，数据库存加密后的值）
        return DigestUtils.md5DigestAsHex(rawSecret.getBytes());
    }

    /**
     * 生成指定长度的随机字符串
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 验证SecretKey（前端传原始值，后端用MD5加密后对比）
     */
    public static boolean verifySecretKey(String rawSecret, String encryptedSecret) {
        if (rawSecret == null || encryptedSecret == null) {
            return false;
        }
        String encryptedRaw = DigestUtils.md5DigestAsHex(rawSecret.getBytes());
        return encryptedRaw.equals(encryptedSecret);
    }

}
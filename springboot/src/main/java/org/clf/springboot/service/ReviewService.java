package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.ReviewResult;
import org.clf.springboot.config.RabbitMqConfig;
import org.clf.springboot.utils.MinIOUtils;
import org.clf.springboot.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ReviewService{

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

    // 幂等性Key前缀
    private static final String IDEMPOTENT_KEY_PREFIX = "review:idempotent:";

    @Value("${minio.redisKey}")
    private String redisPrefix;

    @Resource
    private MinIOUtils minIOUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private TokenUtils tokenUtils;

    @Transactional(rollbackFor = Exception.class)
    public Result pictureView(MultipartFile file, String requestId) {
        try {
            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf(".")); // 提取后缀（如.jpg）
            String objectName = "user/upload/" + UUID.randomUUID().toString().replace("-", "") + suffix;
            // 上传图片到minio，此处objectName与imageId有异
            minIOUtils.uploadFile(file, objectName);
            String preSignedUrl = minIOUtils.getPresignedUrl(objectName);

            // 获取当前登录用户id
            Long userId = tokenUtils.getCurrentUserId();

            // 生成图片id
            String imageId = UUID.randomUUID().toString().replace("-", "");

            // 存储该用户所有图片id
            String imageListKey = redisPrefix + userId;
            // 单张图片详情
            String imageDetailKey = redisPrefix + userId + imageId;

            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("preSignedUrl", preSignedUrl);
            saveMap.put("objectName", objectName);

            stringRedisTemplate.opsForHash().putAll(imageDetailKey, saveMap);

            stringRedisTemplate.opsForSet().add(imageListKey, imageId);

            //设置过期时间（7天），列表和详情键同步过期
            stringRedisTemplate.expire(imageListKey, 7, TimeUnit.DAYS);
            stringRedisTemplate.expire(imageDetailKey, 7, TimeUnit.DAYS);
            //TODO 对接pythonYolo接口

            // 这里yolo会返回一个准确值
            Double yoloResult = 0.48;
            try {
                // 根据返回值设定一个阈值判断是否需要人工审核，打入死信队列中
                if (yoloResult < 0.5) {
                    rabbitTemplate.convertAndSend(RabbitMqConfig.DEAD_LETTER_EXCHANGE_NAME,
                            RabbitMqConfig.DEAD_LETTER_ROUTING_KEY,
                            saveMap);
                }
                rabbitTemplate.convertAndSend(RabbitMqConfig.BUSINESS_EXCHANGE_NAME, RabbitMqConfig.BUSINESS_ROUTING_KEY, saveMap);
            } catch (AmqpException e) {
                LOGGER.error("消息发送到业务队列失败，转发到死信队列", e);
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.DEAD_LETTER_EXCHANGE_NAME,
                        RabbitMqConfig.DEAD_LETTER_ROUTING_KEY,
                        saveMap
                );
            }

        } catch (Exception e) {
            LOGGER.error("图片审核失败", e);
            return Result.error();
        }
        return Result.success();
    }
}

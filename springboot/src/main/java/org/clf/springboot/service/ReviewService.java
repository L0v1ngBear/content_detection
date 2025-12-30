package org.clf.springboot.service;

import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.ReviewResult;
import org.clf.springboot.config.RabbitMqConfig;
import org.clf.springboot.dto.PictureReviewDTO;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.utils.MinIOUtils;
import org.clf.springboot.utils.RedisUtils;
import org.clf.springboot.utils.TokenUtils;
import org.clf.springboot.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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

    private static final int REDIS_EXPIRE_TIME = 7;

    // redis自增序列key
    private static final String IMAGE_ID_SEQ_KEY = "image_info:id:seq";
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

    @Resource
    private RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void pictureView(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new CustomException("401", "图片不存在");
            }
            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf(".")); // 提取后缀（如.jpg）
            String objectName = "user/upload/" + UUID.randomUUID().toString().replace("-", "") + suffix;
            // 上传图片到minio，此处objectName与imageId有异
            minIOUtils.uploadFile(file, objectName);
            String preSignedUrl = minIOUtils.getPresignedUrl(objectName);

            // 获取当前登录用户id
            Long userId = UserContextHolder.getUserId();

            // 生成图片id
            String imageId = UUID.randomUUID().toString().replace("-", "");

            // 存储该用户所有图片id
            String imageListKey = redisPrefix + userId;
            // 单张图片详情
            String imageDetailKey = redisPrefix + userId + imageId;

            // 封装图片信息
            PictureReviewDTO imageInfo = buildImageInfoMap(preSignedUrl, objectName, imageId, userId);

            // 存储到redis中
            extracted(imageDetailKey, imageId, objectName, preSignedUrl);

            // TODO 消息队列将图片入库
            rabbitTemplate.convertAndSend(RabbitMqConfig.MYSQL_EXCHANGE_NAME,
                    RabbitMqConfig.MYSQL_ROUTING_KEY,
                    imageInfo,
                    new CorrelationData(UUID.randomUUID().toString()));

            // 添加图片列表图片id
            addImageId(imageListKey, imageId);

            stringRedisTemplate.expire(imageDetailKey, REDIS_EXPIRE_TIME, TimeUnit.DAYS);

            LOGGER.info("图片上传成功，待审核, imageId={}, userId={}", imageId, userId);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            // 将生成的唯一消息id存入redis，便于检查是否重复消费
            stringRedisTemplate.opsForValue().set(IDEMPOTENT_KEY_PREFIX + correlationData.getId(), "1");
            stringRedisTemplate.expire(IDEMPOTENT_KEY_PREFIX + correlationData.getId(), REDIS_EXPIRE_TIME, TimeUnit.DAYS);

            // 发送业务信息待审核
            rabbitTemplate.convertAndSend(RabbitMqConfig.BUSINESS_EXCHANGE_NAME,
                    RabbitMqConfig.BUSINESS_ROUTING_KEY,
                    imageInfo,
                    new CorrelationData(UUID.randomUUID().toString().replace("-", "")));

            //TODO 实现yolo审核消费者

//            // 这里yolo会返回一个准确值
//            double yoloResult = 0.48;
//            try {
//                // 根据返回值设定一个阈值判断是否需要人工审核，打入死信队列中
//                if (yoloResult < 0.5) {
//                    rabbitTemplate.convertAndSend(RabbitMqConfig.DEAD_LETTER_EXCHANGE_NAME,
//                            RabbitMqConfig.DEAD_LETTER_ROUTING_KEY,
//                            saveMap);
//                }
//                rabbitTemplate.convertAndSend(RabbitMqConfig.BUSINESS_EXCHANGE_NAME, RabbitMqConfig.BUSINESS_ROUTING_KEY, saveMap);
//            } catch (AmqpException e) {
//                LOGGER.error("消息发送到业务队列失败，转发到死信队列", e);
//                rabbitTemplate.convertAndSend(
//                        RabbitMqConfig.DEAD_LETTER_EXCHANGE_NAME,
//                        RabbitMqConfig.DEAD_LETTER_ROUTING_KEY,
//                        saveMap
//                );
//            }
        } catch (Exception e) {
            LOGGER.error("图片审核失败", e);
            throw new CustomException("500", "系统异常", e);
        }
    }

    private void extracted(String imageDetailKey, String imageId, String objectName, String preSignedUrl) {
        stringRedisTemplate.opsForHash().put(imageDetailKey, "id", String.valueOf(redisUtils.getId(IMAGE_ID_SEQ_KEY)));
        stringRedisTemplate.opsForHash().put(imageDetailKey, "imageId", imageId);
        stringRedisTemplate.opsForHash().put(imageDetailKey, "objectName", objectName);
        stringRedisTemplate.opsForHash().put(imageDetailKey, "preSignedUrl", preSignedUrl);
        stringRedisTemplate.opsForHash().put(imageDetailKey, "status", "PENDING");
    }

    private PictureReviewDTO buildImageInfoMap(String preSignedUrl, String objectName, String imageId, Long userId) {
        PictureReviewDTO pictureReviewDTO = new PictureReviewDTO();
        pictureReviewDTO.setObjectName(objectName);
        pictureReviewDTO.setUserId(userId);
        pictureReviewDTO.setPreSignedUrl(preSignedUrl);
        pictureReviewDTO.setImageId(imageId);
        return pictureReviewDTO;
    }

    private void addImageId(String imageListKey, String imageId) {
        // 存储图片ID，score为创建时间戳
        stringRedisTemplate.opsForZSet().add(imageListKey, imageId, System.currentTimeMillis());
        // 为ZSet设置一个兜底的过期时间（防止定时任务失效）
        stringRedisTemplate.expire(imageListKey, REDIS_EXPIRE_TIME + 1, TimeUnit.DAYS);
    }

}

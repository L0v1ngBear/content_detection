package org.clf.springboot.service;

import cn.hutool.core.date.DateTime;
import jakarta.annotation.Resource;
import org.clf.springboot.common.Result;
import org.clf.springboot.common.ReviewResult;
import org.clf.springboot.common.enums.ResultCodeEnum;
import org.clf.springboot.config.RabbitMqConfig;
import org.clf.springboot.dto.PictureResponseDTO;
import org.clf.springboot.dto.PictureReviewDTO;
import org.clf.springboot.entity.DetectHistory;
import org.clf.springboot.exception.CustomException;
import org.clf.springboot.mapper.DetectHistoryMapper;
import org.clf.springboot.utils.MinIOUtils;
import org.clf.springboot.utils.RedisUtils;
import org.clf.springboot.utils.TokenUtils;
import org.clf.springboot.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private static final String NOW_COUNT = "now_count";

    // 保存审核次数
    private static final int SHARED_COUNT = 16;
    private static final String REDIS_KEY_PREFIX = "stat_";
    private static final String STAT_TYPE_PICTURE_REVIEW = "picture_review_count";
    private static final long REDIS_KEY_EXPIRE_SECONDS = 32 * 24 * 3600;

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

    @Resource
    private DetectHistoryMapper detectHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public String pictureView(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new CustomException("图片不存在");
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
            DetectHistory imageInfo = buildImageInfoMap(preSignedUrl, objectName, imageId, userId);

            // 发送到消息队列，存入mysql
            rabbitTemplate.convertAndSend(RabbitMqConfig.MYSQL_EXCHANGE_NAME,
                    RabbitMqConfig.MYSQL_ROUTING_KEY,
                    imageInfo,
                    new CorrelationData(UUID.randomUUID().toString()));

            // 将用户最近上传图片存入redis
            addImageId(imageListKey, imageId);

            // 设置过期时间
            stringRedisTemplate.expire(imageDetailKey, REDIS_EXPIRE_TIME, TimeUnit.DAYS);

            LOGGER.info("图片上传成功，待审核, imageId={}, userId={}", imageId, userId);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            // 将生成的唯一消息id存入redis，便于检查是否重复消费
            stringRedisTemplate.opsForValue().set(IDEMPOTENT_KEY_PREFIX + correlationData.getId(), "1");
            stringRedisTemplate.expire(IDEMPOTENT_KEY_PREFIX + correlationData.getId(), REDIS_EXPIRE_TIME, TimeUnit.DAYS);

            // 发送业务信息待审核, tobusiness消费队列
            rabbitTemplate.convertAndSend(RabbitMqConfig.BUSINESS_EXCHANGE_NAME,
                    RabbitMqConfig.BUSINESS_ROUTING_KEY,
                    imageInfo,
                    new CorrelationData(UUID.randomUUID().toString().replace("-", "")));

            // 实时检测数量加1
            stringRedisTemplate.opsForValue().increment(NOW_COUNT);


            return imageId;
        } catch (Exception e) {
            LOGGER.error("图片审核失败", e);
            throw new CustomException(500, "系统异常", e);
        }
    }

    private DetectHistory buildImageInfoMap(String preSignedUrl, String objectName, String imageId, Long userId) {
        DetectHistory dto = new DetectHistory();
        dto.setStatus(1);
        dto.setUserId(userId);
        dto.setObjectName(objectName);
        dto.setDetectType("image");
        dto.setPresignedUrl(preSignedUrl);
        dto.setObjectId(imageId);
        return dto;
    }

    private void addImageId(String imageListKey, String imageId) {
        // 存储图片ID，score为创建时间戳
        stringRedisTemplate.opsForZSet().add(imageListKey, imageId, System.currentTimeMillis());
        stringRedisTemplate.expire(imageListKey, REDIS_EXPIRE_TIME + 1, TimeUnit.DAYS);
    }

    public String videoView(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new CustomException("文件不存在");
            }
            // TODO 完善视频审核
            return file.getOriginalFilename();
        } catch (Exception e) {
            throw new CustomException(500, "系统异常");
        }
    }

    public void savePicReview(Long userId) {
        try {
            if (userId == null) {
                throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
            }
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            int sharedIndex = Math.abs(userId.hashCode() % SHARED_COUNT);
            String redisKey = String.format("%s%s_%s_%d", REDIS_KEY_PREFIX, STAT_TYPE_PICTURE_REVIEW, currentMonth, sharedIndex);
            String redisField = userId.toString();

            stringRedisTemplate.opsForHash().increment(redisKey, redisField, 1);

            // 3. 仅首次设置过期时间（避免重复调用EXPIRE）
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(redisKey))) {
                stringRedisTemplate.expire(redisKey, Duration.ofDays(REDIS_KEY_EXPIRE_SECONDS));
            }
        } catch (Exception e) {
            LOGGER.error("统计用户图片审核数量失败");
        }
    }

    public PictureResponseDTO getPictureResult(String imageId, Long userId) {
        DetectHistory detectHistory = detectHistoryMapper.selectByImageId(imageId, userId);
        PictureResponseDTO resDto = new PictureResponseDTO();
        resDto.setTaskId(detectHistory.getObjectId());
        resDto.setStatus(detectHistory.getStatus());
        resDto.setConfidence(detectHistory.getConfidence());
        resDto.setViolationType(detectHistory.getViolationType());
        return resDto;
    }
}

package org.clf.springboot.rabbitListener;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.clf.springboot.entity.DetectHistory;
import org.clf.springboot.entity.Msg;
import org.clf.springboot.mapper.DetectHistoryMapper;
import org.clf.springboot.mapper.MsgMapper;
import org.clf.springboot.utils.ValidationUtils;
import org.clf.springboot.websocket.ImageResultWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;


@Component
public class RabbitConsumerListener {

    @Value("${yolo.imageUrl}")
    private String IMAGE_YOLO_API_URL;

    @Value("${yolo.videoUrl}")
    private String VIDEO_YOLO_API_URL;

    @Value("${spring.rabbitmq.retry_count}")
    private int RETRY_COUNT;

    @Resource
    private DetectHistoryMapper detectHistoryMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RabbitConsumerListener.class);

    private static final Map<String, String> NORMAL_CLASSES = Map.of(
            "Normal", "正常",
            "Adult", "色情",
            "Violent", "暴力"
    );
    // 超时时间
    private static final int TIMEOUT_SECONDS = 30;

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)    // 连接超时
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)       // 读取超时
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)      // 写入超时
            .retryOnConnectionFailure(true)          // 连接失败自动重试
            .build();

    @Resource
    private ImageResultWebSocket imageResultWebSocket;

    @Resource
    private MsgMapper msgMapper;

    @RabbitListener(queues = "business.queue")
    public void businessReview(DetectHistory detectHistory,
                              Message message,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String proId = detectHistory.getObjectId();
        String proUrl = detectHistory.getPresignedUrl();
        String fileName = detectHistory.getFileName();
        Long userId = detectHistory.getUserId();
        String detectType = detectHistory.getDetectType();
        logger.info("开始处理审核任务，ID：{}，MinIO链接：{}", proId, proUrl);

        try {
            JSONObject yoloResult = classifyByUrl(proUrl, proId, detectType);
            if (yoloResult == null) {
                throw new RuntimeException("YOLO结果调用失败，id: " + proId);
            }

            int code = yoloResult.getIntValue("code");
            if (code != 200) {
                throw new RuntimeException(String.format(
                        "YOLO审核失败，ID：%s，错误码：%d，错误信息：%s",
                        proId, code, yoloResult.getString("msg")));
            }

            JSONObject data = yoloResult.getJSONObject("data");
            String finalClass = data.getString("final_class");
            double finalProb = data.getDoubleValue("final_prob");
            double normalProb = data.getJSONObject("detail_probs").getDoubleValue("Normal");
            double adultProb = data.getJSONObject("detail_probs").getDoubleValue("Adult");
            double violentProb = data.getJSONObject("detail_probs").getDoubleValue("Violent");

            logger.info("项目{} YOLO审核完成：最终分类={}（概率{}%），Normal概率{}%，Adult概率{}%，Violent概率{}%",
                    proId, finalClass, finalProb, normalProb, adultProb, violentProb);

            detectHistory.setDetectTime(new Date());
            /**
             * 检测状态（0-待检测，1-检测中，2-检测成功，3-检测失败）
             */
            detectHistory.setStatus(2);
            detectHistory.setViolationType(finalClass);
            detectHistory.setConfidence(finalProb / 100);

            detectHistoryMapper.updateStatusById(detectHistory);
            logger.info("入库成功，项目id{}", proId);

            // 推送消息到websocket
            Msg msg = new Msg();
            msg.setType("DETECT");
            msg.setUserId(String.valueOf(detectHistory.getUserId()));
            String classType = NORMAL_CLASSES.get(finalClass);
            String content = String.format("项目AI检测完成：检测项目名称:%s，类型:%s，违规概率:%s%%", fileName, classType, finalProb);
            msg.setContent(content);
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            imageResultWebSocket.sendMessage(String.valueOf(userId), msg);

            msgMapper.insert(msg);
            logger.info("审核消息入库成功，消息id{}", msg.getId());
        } catch (Exception e) {

            detectHistory.setStatus(3);
            detectHistoryMapper.updateErrorStatus(detectHistory);
            // 异常转入人工审核
        }

        // TODO 处理成特定格式
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = "mysql.queue")
    public void saveMysql(DetectHistory dto,
                          Message message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        // 1. 初始化基础变量，避免空指针
        String imageId = dto != null ? dto.getObjectId() : "未知ID";
        Integer retryCount = 0;

        // 安全获取重试次数
        try {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            retryCount = headers.get("retry_count") != null ?
                    Integer.parseInt(headers.get("retry_count").toString()) : 0;
        } catch (Exception e) {
            logger.warn("获取重试次数失败，默认重置为0，图片ID：{}", imageId, e);
            retryCount = 0;
        }

        // 只在方法开头设置一次basicQos，保证单条消费
        channel.basicQos(1);

        try {
            // 2. 校验DTO非空
            if (dto == null) {
                throw new IllegalArgumentException("dto 为空，图片ID：" + imageId);
            }

            // 3. 幂等性校验：避免重复入库（关键！）
            if (detectHistoryMapper.existsByImageId(imageId) > 0) {
                logger.info("图片已入库，无需重复处理，图片ID：{}", imageId);
                channel.basicAck(deliverTag, false);
                return;
            }

            String errorMsg = ValidationUtils.validateWithMsg(dto);
            if (errorMsg != null) {
                logger.error("图片参数校验失败，不重试，图片ID：{}，错误信息：{}", imageId, errorMsg);
                channel.basicNack(deliverTag, false, false);
                return;
            }

            // 5. 执行入库
            detectHistoryMapper.insert(dto);
            channel.basicAck(deliverTag, false);
            logger.info("消息入库成功，图片ID：{}，重试次数：{}", imageId, retryCount);

        } catch (Exception e) {
            logger.error("图片入库异常，图片ID：{}，当前重试次数：{}", imageId, retryCount, e);

            // 核心修复：替换basicNack重入队为手动发消息
            if (retryCount < RETRY_COUNT) {
                // 第一步：确认当前消息（从队列中移除）
                channel.basicAck(deliverTag, false);

                // 第二步：构建新消息，携带更新后的重试次数
                Message newMessage = MessageBuilder.fromMessage(message)
                        .setHeader("retry_count", retryCount + 1) // 次数+1
                        .build();

                // 第三步：手动发送新消息到原队列（可选延迟，避免高频重试）
                Integer finalRetryCount = retryCount;

                rabbitTemplate.convertAndSend("mysql.queue", newMessage, msg -> {
                    // 指数退避：第1次重试延迟1s，第2次2s，第3次4s
                    msg.getMessageProperties().setDelay(RETRY_COUNT * (finalRetryCount + 1));
                    return msg;
                });

                logger.warn("图片入库失败，将重试，图片ID：{}，剩余重试次数：{}",
                        imageId, RETRY_COUNT - retryCount - 1);
            } else {
                // 重试次数达上限：拒绝且不重发（彻底从队列移除）
                channel.basicNack(deliverTag, false, false);
                logger.error("图片入库重试次数达上限，丢弃消息，图片ID：{}", imageId);
                // 可选：发送到死信队列，便于后续排查
                // sendToDeadLetterQueue(pictureReviewDTO, e, imageId);
            }
        }
    }

    private JSONObject classifyByUrl(String Url, String imageId, String detectType) {
        if (Url == null || !Url.startsWith("http")) {
            logger.error("无效的MinIO链接：{}", Url);
            return null;
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("url", Url);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody.toString()
        );

        Request request = new Request.Builder()
                .url(detectType.equals("image") ? IMAGE_YOLO_API_URL : VIDEO_YOLO_API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // 4. 发送请求并解析结果
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                solveYoloException(imageId);
                logger.error("YOLO接口调用失败，状态码：{}，链接：{}", response.code(), Url);
                if (response.body() != null) {
                    logger.error("YOLO接口错误响应：{}", response.body().string());
                }
                return null;
            }

            // 解析JSON响应
            String responseStr = Objects.requireNonNull(response.body()).string();
            return JSONObject.parseObject(responseStr);

        } catch (IOException e) {
            solveYoloException(imageId);
            logger.error("调用YOLO接口IO异常，链接：{}", Url, e);
            return null;
        }
    }


    private void solveYoloException(String imageId) {
        detectHistoryMapper.updateErrorStatusByImageId(imageId);
    }
}


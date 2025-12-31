package org.clf.springboot.rabbitListener;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.clf.springboot.dto.PictureReviewDTO;
import org.clf.springboot.entity.Picture;
import org.clf.springboot.mapper.PictureMapper;
import org.clf.springboot.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

@Component
public class RabbitConsumerListener {

    @Value("${spring.rabbitmq.retry_count}")
    private static int RETRY_COUNT;

    @Resource
    private PictureMapper pictureMapper;

    private static final Logger logger = LoggerFactory.getLogger(RabbitConsumerListener.class);

    @RabbitListener(queues = "picture.queue")
    public void pictureReview(PictureReviewDTO pictureReviewDTO,
                              Message message,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        // TODO 接入yolo审核
        System.out.println("pictureReviewDTO: " + pictureReviewDTO);
        System.out.println("deliveryTag: " + deliveryTag);
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = "mysql.queue")
    public void saveMysql(PictureReviewDTO pictureReviewDTO,
                          Message message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        Picture picture = new Picture();
        Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("retry_count");
        retryCount = retryCount == null ? 0 : retryCount;
        try {
            BeanUtils.copyProperties(pictureReviewDTO, picture);
            String errorMsg = ValidationUtils.validateWithMsg(picture);
            if (errorMsg != null) {
                channel.basicNack(deliverTag, false, false);
                logger.error(errorMsg);
            }
            pictureMapper.insert(picture);
            channel.basicAck(deliverTag, false);
            logger.info("消息入库成功, 图片id{}", picture.getImageId());
        } catch (Exception e) {
            if (retryCount < RETRY_COUNT) {
                message.getMessageProperties().setHeader("retry_count", retryCount + 1);
                channel.basicNack(deliverTag, false, true);
                logger.warn("消息入库失败,图片id{}", picture.getImageId());
            } else {
                channel.basicNack(deliverTag, false, false);
                logger.error("重试次数达到上限,图片id{}", picture.getImageId());
            }
            logger.error("入库失败", e);
        }
    }

}


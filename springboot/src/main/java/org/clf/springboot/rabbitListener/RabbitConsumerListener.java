package org.clf.springboot.rabbitListener;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.clf.springboot.dto.PictureReviewDTO;
import org.clf.springboot.entity.Picture;
import org.clf.springboot.mapper.PictureMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitConsumerListener {

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


    // TODO 需要优化
    @RabbitListener(queues = "mysql.queue")
    public void saveMysql(PictureReviewDTO pictureReviewDTO,
                          Message message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewDTO, picture);
        try {
            pictureMapper.insert(picture);
            channel.basicAck(deliverTag, false);
        } catch (Exception e) {
            channel.basicNack(deliverTag, false, true);
        }

    }
}

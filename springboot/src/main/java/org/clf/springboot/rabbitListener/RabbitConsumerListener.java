package org.clf.springboot.rabbitListener;

import com.rabbitmq.client.Channel;
import org.clf.springboot.dto.PictureReviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitConsumerListener {

    Logger logger = LoggerFactory.getLogger(RabbitConsumerListener.class);

    @RabbitListener(queues = "picture.queue")
    // TODO 实现监听器
    public void pictureReview(PictureReviewDTO pictureReviewDTO,
                              Message message,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        System.out.println("pictureReviewDTO: " + pictureReviewDTO);
        System.out.println("deliveryTag: " + deliveryTag);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = "mysql.queue")
    public void saveMysql(Message message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        logger.info(message.getMessageProperties().toString());

    }
}

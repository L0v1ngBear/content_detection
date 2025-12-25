package org.clf.springboot.rabbitListener;

import com.rabbitmq.client.Channel;
import org.clf.springboot.dto.PictureReviewDTO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PictureReviewListener {
    @RabbitListener(queues = "picture.queue")
    public void pictureReview(PictureReviewDTO pictureReviewDTO,
                              Message message,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        System.out.println("pictureReviewDTO: " + pictureReviewDTO);
        System.out.println("deliveryTag: " + deliveryTag);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

package org.clf.springboot.rabbitListener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PictureReviewListener {
    @RabbitListener(queues = "picture.queue")
    public void pictureReview(Message message){}
}

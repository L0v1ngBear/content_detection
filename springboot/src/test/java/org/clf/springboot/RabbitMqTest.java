package org.clf.springboot; // 替换为你的实际包名

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * RabbitMQ可用性测试类
 * @SpringBootTest：启动Spring容器，加载配置
 */
@SpringBootTest
public class RabbitMqTest {

    // 注入RabbitTemplate，用于操作RabbitMQ
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 测试RabbitMQ连接与消息发送
     * 若无异常抛出，说明连接正常
     */
    @Test
    public void testSendMessage() {
        // 1. 测试消息内容
        String testMessage = "Hello RabbitMQ from Spring Boot Test!";
        // 2. 发送消息到测试队列（队列不存在会自动创建）
        rabbitTemplate.convertAndSend("picture.queue", testMessage);
        // 3. 打印日志，确认发送成功
        System.out.println("测试消息发送完成，内容：" + testMessage);
    }

    /**
     * 测试死信队列消息发送（可选，根据你的业务添加）
     */

    public void testSendDeadLetterMessage() {
        String deadLetterMsg = "Test Dead Letter Queue!";
        rabbitTemplate.convertAndSend("picture.dlx.exchange", "picture.dlx.key", deadLetterMsg);
        System.out.println("死信队列测试消息发送完成");
    }
}
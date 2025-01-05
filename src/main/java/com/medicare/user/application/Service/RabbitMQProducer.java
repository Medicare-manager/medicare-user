package com.medicare.user.application.Service;

import com.medicare.user.domain.model.EmailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter()); // Configura o conversor JSON

    }

    public void sendToEmailQueue(String email, String subject, String body) {
        EmailMessage emailMessage = new EmailMessage(email, subject, body);
        System.out.println("enviando mensagem para rabbit "+emailMessage);
        // O RabbitTemplate automaticamente converte o EmailMessage para JSON
        rabbitTemplate.convertAndSend("login-email-queue", emailMessage);
    }

    /*private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToEmailQueue(String email, String subject, String message) {
        EmailMessage emailMessage = new EmailMessage(email, subject, message);
        rabbitTemplate.convertAndSend("login-email-queue", emailMessage);
    }*/
}

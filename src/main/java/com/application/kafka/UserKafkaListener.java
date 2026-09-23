package com.application.kafka;

import com.application.model.UserDto;
import com.application.model.UserMessage;
import com.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserKafkaListener {

    private final UserService userService;

    @KafkaListener(
            topics = "TEST.IN.TOPIC",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(@Payload UserMessage userMessage, Acknowledgment acknowledgment) {
        try {
            log.info("Received message from Kafka: firstName={}, lastName={}, email={}",
                    userMessage.getFirstName(), userMessage.getLastName(), userMessage.getEmail());

            // Преобразуем сообщение в DTO для создания пользователя
            UserDto userDto = new UserDto(
                    null,  // id
                    userMessage.getLastName(),
                    userMessage.getFirstName(),
                    userMessage.getEmail(),
                    null,  // createdAt
                    null   // updatedAt
            );

            // Создаем пользователя через сервис
            userService.createUser(userDto);

            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge();

            log.info("User created successfully from Kafka message");

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
            // В зависимости от требований:
            // - Можно не подтверждать (acknowledgment) - сообщение будет обработано снова
            // - Можно отправить в DLQ (Dead Letter Queue)
            // - Можно залогировать и продолжить
            throw new RuntimeException("Failed to process Kafka message", e);
        }
    }
}
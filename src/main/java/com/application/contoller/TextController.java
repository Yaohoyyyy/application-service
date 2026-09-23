package com.application.contoller;

import com.application.model.TextRequest;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
@Slf4j
public class TextController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AsyncPublisher(operation = @AsyncOperation(
            channelName = "TEST.OUT.TOPIC",
            description = "Send text payload to the TEST.OUT.TOPIC Kafka topic"))
    @KafkaAsyncOperationBinding
    public void sendText(@RequestBody TextRequest request) {
        log.info("Sending text to Kafka topic TEST.OUT.TOPIC: {}", request.text());
        kafkaTemplate.send("TEST.OUT.TOPIC", request.text());
    }
}

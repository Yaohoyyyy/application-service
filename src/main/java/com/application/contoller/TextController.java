package com.application.contoller;

import com.application.model.TextRequest;
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
    public void sendText(@RequestBody TextRequest request) {
        log.info("Sending text to Kafka topic TEST.OUT.TOPIC: {}", request.text());
        kafkaTemplate.send("TEST.OUT.TOPIC", request.text());
    }
}

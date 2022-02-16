package com.baeldung.kafka.streams;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String key, String message) {

        kafkaTemplate.send("input-topic", key, message)
          .addCallback(
            result -> log.info("Message with key: {} sent to topic: {}", key, message),
            ex -> log.error("Failed to send message", ex)
          );
    }
}

package com.example.inventoryservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T> void publish(String topic, String key, T event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.info("Event published to topic [{}] with key [{}]: {}", topic, key, event);
        } catch (Exception e) {
            log.error("Failed to publish event to topic [{}] with key [{}]: {}", topic, key, event, e);
        }
    }
}
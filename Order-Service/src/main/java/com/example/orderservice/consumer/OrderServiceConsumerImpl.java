package com.example.orderservice.consumer;

import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderServiceConsumerImpl implements OrderServiceConsumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "inventory-response")
    @Override
    public void listenInventoryResponse(String message) {
        try {
            InventoryResponseEvent event = objectMapper.readValue(message, InventoryResponseEvent.class);
            orderService.listenInventoryResponse(event);
        } catch (Exception e) {
            log.error("Failed to parse InventoryResponseEvent from message: {}", message, e);
        }
    }
}


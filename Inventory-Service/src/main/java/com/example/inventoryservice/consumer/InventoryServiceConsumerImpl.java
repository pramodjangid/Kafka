package com.example.inventoryservice.consumer;

import com.example.inventoryservice.constants.Constants;
import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryServiceConsumerImpl implements InventoryServiceConsumer {

    @Autowired
    private InventoryService inventoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @KafkaListener(topics = Constants.ORDER_PLACED)
    public void handleOrderEvent(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            inventoryService.handleOrderEvent(event);
        } catch (Exception e) {
            log.error("Failed to parse OrderPlacedEvent from message: {}", message, e);
        }
    }

    @Override
    @KafkaListener(topics = Constants.ORDER_CONFIRMED)
    public void handleOrderConfirmationEvent(String message) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(message, OrderConfirmedEvent.class);
            inventoryService.handleOrderConfirmationEvent(event);
        } catch (Exception e) {
            log.error("Failed to parse OrderConfirmedEvent from message: {}", message, e);
        }
    }
}

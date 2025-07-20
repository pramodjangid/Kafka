package com.example.inventoryservice.service.listener;

import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConfirmationListener {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "order-confirmed-events", groupId = "inventory-service")
    public void handleConfirmation(String message) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(message, OrderConfirmedEvent.class);
            inventoryRepository.reduceQuantity(event.getProductId(), event.getQuantity());
        } catch (Exception e) {
            log.error("Failed to handle order confirmation", e);
        }
    }

}

package com.example.inventoryservice.listeners;

import com.example.inventoryservice.constants.KafkaTopics;
import com.example.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryServiceListenerImpl implements InventoryServiceListener {

    @Autowired
    private InventoryService inventoryService;

    @KafkaListener(topics = KafkaTopics.ORDER_PLACED, groupId = "inventory-service")
    @Override
    public void handleOrderEvent(String message) {
        inventoryService.handleOrderEvent(message);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, groupId = "inventory-service")
    @Override
    public void handleOrderConfirmationEvent(String message) {
        inventoryService.handleOrderConfirmationEvent(message);
    }
}

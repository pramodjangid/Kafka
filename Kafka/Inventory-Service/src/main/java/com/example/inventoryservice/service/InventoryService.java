package com.example.inventoryservice.service;

import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
import com.example.inventoryservice.producer.InventoryServiceProducer;
import com.example.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InventoryService {

    @Autowired
    private InventoryServiceProducer inventoryServiceProducer;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    public void handleOrderEvent(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);

            if (!processedOrderIds.add(event.getOrderId())) {
                log.warn("Duplicate order skipped: {}", event.getOrderId());
                return;
            }

            boolean available = inventoryRepository.findById(event.getProductId())
                    .map(inv -> inv.getQuantity() >= event.getQuantity())
                    .orElse(false);

            InventoryResponseEvent response = new InventoryResponseEvent(
                    event.getOrderId(), available, event.getProductId(), event.getQuantity()
            );

            inventoryServiceProducer.sendInventoryResponse(response);

        } catch (Exception e) {
            log.error("Failed to process order-event", e);
            throw new RuntimeException("Kafka processing failed", e);
        }
    }

    public void handleOrderConfirmationEvent(String message) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(message, OrderConfirmedEvent.class);

            inventoryRepository.findById(event.getProductId()).ifPresent(inventory -> {
                int currentQuantity = inventory.getQuantity();
                int newQuantity = currentQuantity - event.getQuantity();
                inventory.setQuantity(Math.max(newQuantity, 0));
                inventoryRepository.save(inventory);
            });

        } catch (Exception e) {
            log.error("Failed to handle order confirmation", e);
        }
    }

}

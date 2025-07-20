package com.example.inventoryservice.service.listener;

import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
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
public class InventoryListener {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderEvent(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);

            if (!processedOrderIds.add(event.getOrderId())) {
                log.warn("Duplicate order skipped: {}", event.getOrderId());
                return;
            }

            boolean available = inventoryRepository.isAvailable(event.getProductId(), event.getQuantity()).orElse(false);

            InventoryResponseEvent inventoryResponseEvent = new InventoryResponseEvent(event.getOrderId(), available, event.getProductId(), event.getQuantity());

            kafkaTemplate.send("inventory-response", event.getOrderId(), inventoryResponseEvent);
        } catch (Exception e) {
            log.error("Failed to process order-event", e);
            throw new RuntimeException("Kafka processing failed", e);
        }
    }

}
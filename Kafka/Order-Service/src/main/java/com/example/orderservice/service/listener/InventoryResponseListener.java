package com.example.orderservice.service.listener;

import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.service.OrderResponseStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryResponseListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderResponseStore responseStore;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-response", groupId = "order-service")
    public void listenInventoryResponse(String message) {
        try {
            InventoryResponseEvent event = objectMapper.readValue(message, InventoryResponseEvent.class);
            if (event.isAvailable()) {
                kafkaTemplate.send(
                        "order-confirmed-events",
                        event.getOrderId(),
                        new OrderConfirmedEvent(event.getOrderId(), event.getProductId(), event.getQuantity())
                );
                responseStore.complete(event.getOrderId(), "Order placed successfully");
            } else {
                responseStore.complete(event.getOrderId(), "Product not available in inventory");
            }
        } catch (Exception e) {
            log.error("Failed to process inventory response", e);
            String fallbackOrderId = extractOrderIdFromJson(message);
            responseStore.completeExceptionally(fallbackOrderId, e);
        }
    }

    private String extractOrderIdFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(json).get("orderId").asText();
        } catch (Exception ex) {
            return "unknown-order";
        }
    }
}
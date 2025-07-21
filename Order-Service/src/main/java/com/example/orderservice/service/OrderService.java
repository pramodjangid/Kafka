package com.example.orderservice.service;

import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.producer.OrderServiceProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderServiceProducer orderServiceProducer;

    @Autowired
    private OrderResponseStore responseStore;


    public ResponseEntity<String> placeOrder(OrderRequest request) {
        OrderPlacedEvent event = new OrderPlacedEvent(request.getOrderId(), request.getProductId(), request.getQuantity());

        CompletableFuture<String> future = new CompletableFuture<>();
        responseStore.store(request.getOrderId(), future);

        orderServiceProducer.sendOrderPlacedEvent(event);

        try {
            String result = future.get(5, TimeUnit.SECONDS);
            return ResponseEntity.ok(result);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Inventory processing failed: " + cause.getMessage());
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("Timed out waiting for inventory response");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    public void listenInventoryResponse(String message) {
        System.out.println("I am here 6"+ message);

        try {
            InventoryResponseEvent event = objectMapper.readValue(message, InventoryResponseEvent.class);
            log.info("I am here 1 {}", event);
            if (event.isAvailable()) {
                OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(event.getOrderId(), event.getProductId(), event.getQuantity());
                log.info("I am here 2 {}", orderConfirmedEvent);

                orderServiceProducer.sendOrderConfirmationEvent(orderConfirmedEvent);

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
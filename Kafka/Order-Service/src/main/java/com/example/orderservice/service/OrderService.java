package com.example.orderservice.service;

import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OrderService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderResponseStore responseStore;

    public ResponseEntity<String> placeOrder(OrderRequest request) {
        OrderPlacedEvent event = new OrderPlacedEvent(request.getOrderId(), request.getProductId(), request.getQuantity());

        CompletableFuture<String> future = new CompletableFuture<>();
        responseStore.store(request.getOrderId(), future);

        kafkaTemplate.send("order-events", request.getOrderId(), event);

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
}
package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.producer.OrderServiceProducer;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.util.EventBuilderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrderServiceProducer orderServiceProducer;

    @Autowired
    private OrderRepository orderRepository;

    public ResponseEntity<String> placeOrder(OrderRequest request) {
        Order order = Order.builder()
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();
        orderRepository.save(order);

        OrderPlacedEvent event = EventBuilderUtil.buildOrderPlacedEvent(request.getOrderId(), request.getProductId(), request.getQuantity());
        orderServiceProducer.sendOrderPlacedEvent(event);

        return ResponseEntity.ok("Order placed. Check status via /orders/{orderId}/status");
    }

    public void listenInventoryResponse(InventoryResponseEvent event) {
        log.info("Received inventory response: {}", event);

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("Order not found: {}", event.getOrderId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Order already processed with status: {}", order.getStatus());
            return;
        }

        if (event.isAvailable()) {
            OrderConfirmedEvent confirmation = EventBuilderUtil.buildOrderConfirmedEvent(event.getOrderId(), event.getProductId(), event.getQuantity());
            orderServiceProducer.sendOrderConfirmationEvent(confirmation);
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
        }
    }

    public ResponseEntity<String> getOrderStatus(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok("Order Status: " + order.getStatus()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found with ID: " + orderId));
    }
}
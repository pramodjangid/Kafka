package com.example.orderservice.listeners;

import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceListenerImpl implements OrderServiceListener {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "inventory-response", groupId = "order-service")
    @Override
    public void listenInventoryResponse(String message) {
        System.out.println("I am here 5"+ message);
        orderService.listenInventoryResponse(message);
    }
}


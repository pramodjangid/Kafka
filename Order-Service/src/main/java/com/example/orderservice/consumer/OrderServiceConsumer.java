package com.example.orderservice.consumer;

public interface OrderServiceConsumer {
    void listenInventoryResponse(String message);
}

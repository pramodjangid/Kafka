package com.example.inventoryservice.consumer;

public interface InventoryServiceConsumer {
    void handleOrderEvent(String message);
    void handleOrderConfirmationEvent(String message);
}

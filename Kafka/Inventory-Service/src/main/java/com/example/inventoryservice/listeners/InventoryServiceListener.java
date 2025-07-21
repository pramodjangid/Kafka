package com.example.inventoryservice.listeners;

public interface InventoryServiceListener {
    void handleOrderEvent(String message);
    void handleOrderConfirmationEvent(String message);
}

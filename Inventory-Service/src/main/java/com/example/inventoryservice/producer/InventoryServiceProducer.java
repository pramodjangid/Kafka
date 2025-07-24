package com.example.inventoryservice.producer;

import com.example.inventoryservice.model.InventoryResponseEvent;

public interface InventoryServiceProducer {
    void sendInventoryResponse(InventoryResponseEvent event);
}

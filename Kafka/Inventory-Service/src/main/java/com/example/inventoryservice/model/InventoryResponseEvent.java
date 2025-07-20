package com.example.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryResponseEvent {
    private String orderId;
    private boolean available;
    private String productId;
    private int quantity;
}

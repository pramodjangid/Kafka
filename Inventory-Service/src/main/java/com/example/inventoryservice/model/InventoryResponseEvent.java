package com.example.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InventoryResponseEvent {
    private String orderId;
    private boolean available;
    private String productId;
    private int quantity;
}

package com.example.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent {
    private String orderId;
    private String productId;
    private int quantity;
}

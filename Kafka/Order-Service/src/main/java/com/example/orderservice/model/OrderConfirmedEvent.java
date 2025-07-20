package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderConfirmedEvent {
    private String orderId;
    private String productId;
    private int quantity;
}

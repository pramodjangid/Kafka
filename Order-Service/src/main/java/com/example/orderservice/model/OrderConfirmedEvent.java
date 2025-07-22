package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderConfirmedEvent {
    private String orderId;
    private String productId;
    private int quantity;
}

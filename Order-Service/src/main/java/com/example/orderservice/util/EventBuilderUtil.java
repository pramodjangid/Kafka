package com.example.orderservice.util;

import com.example.orderservice.entity.Order;
import com.example.orderservice.model.*;

public class EventBuilderUtil {

    private EventBuilderUtil() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    public static OrderPlacedEvent buildOrderPlacedEvent(String orderId, String productId, int quantity) {
        return OrderPlacedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    public static OrderConfirmedEvent buildOrderConfirmedEvent(String orderId, String productId, int quantity) {
        return OrderConfirmedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .build();
    }


}


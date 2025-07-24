package com.example.inventoryservice.util;

import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;

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

    public static InventoryResponseEvent buildInventoryResponseEvent(String orderId, boolean available, String productId, int quantity) {
        return InventoryResponseEvent.builder()
                .orderId(orderId)
                .available(available)
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}


package com.example.orderservice.producer;

import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;

public interface OrderServiceProducer {

    void sendOrderPlacedEvent(OrderPlacedEvent event);
    void sendOrderConfirmationEvent(OrderConfirmedEvent event);

}

package com.example.orderservice.producer;

import com.example.orderservice.constants.Constants;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.util.KafkaEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceProducerImpl implements OrderServiceProducer {

    private final KafkaEventPublisher publisher;

    public OrderServiceProducerImpl(KafkaEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        publisher.publish(Constants.ORDER_PLACED, event.getOrderId(), event);
    }

    @Override
    public void sendOrderConfirmationEvent(OrderConfirmedEvent event) {
        publisher.publish(Constants.ORDER_CONFIRMED, event.getOrderId(), event);
    }
}

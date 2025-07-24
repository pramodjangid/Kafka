package com.example.orderservice.producer;

import com.example.orderservice.constants.KafkaTopics;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceProducerImpl implements OrderServiceProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        kafkaTemplate.send(KafkaTopics.ORDER_PLACED, event.getOrderId(), event);
    }

    @Override
    public void sendOrderConfirmationEvent(OrderConfirmedEvent event) {
        System.out.println("I am here 2 {}"+ event);
        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, event.getOrderId(), event);
    }
}

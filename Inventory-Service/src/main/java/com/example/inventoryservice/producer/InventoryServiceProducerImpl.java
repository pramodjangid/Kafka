package com.example.inventoryservice.producer;

import com.example.inventoryservice.constants.KafkaTopics;
import com.example.inventoryservice.model.InventoryResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryServiceProducerImpl implements InventoryServiceProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendInventoryResponse(InventoryResponseEvent event) {
        kafkaTemplate.send(KafkaTopics.INVENTORY_RESPONSE, event.getOrderId(), event);
    }
}

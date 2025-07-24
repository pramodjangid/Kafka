package com.example.inventoryservice.producer;

import com.example.inventoryservice.constants.Constants;
import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.util.KafkaEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InventoryServiceProducerImpl implements InventoryServiceProducer {

    private final KafkaEventPublisher publisher;

    public InventoryServiceProducerImpl(KafkaEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void sendInventoryResponse(InventoryResponseEvent event) {
        publisher.publish(Constants.INVENTORY_RESPONSE, event.getOrderId(), event);
    }
}

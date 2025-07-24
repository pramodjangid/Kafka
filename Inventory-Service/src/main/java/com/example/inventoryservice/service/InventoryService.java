package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
import com.example.inventoryservice.producer.InventoryServiceProducer;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.util.EventBuilderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryService {

    @Autowired
    private InventoryServiceProducer inventoryServiceProducer;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void handleOrderEvent(OrderPlacedEvent event) {

        boolean available = inventoryRepository.findById(event.getProductId())
                .map(inv -> inv.getQuantity() >= event.getQuantity())
                .orElse(false);

        InventoryResponseEvent response = EventBuilderUtil.buildInventoryResponseEvent(event.getOrderId(), available, event.getProductId(), event.getQuantity());

        inventoryServiceProducer.sendInventoryResponse(response);

    }

    public void handleOrderConfirmationEvent(OrderConfirmedEvent event) {
        Inventory inventory = inventoryRepository.findById(event.getProductId())
                .orElseThrow(() -> new IllegalStateException("Inventory not found for productId: " + event.getProductId()));

        int currentQuantity = inventory.getQuantity();
        int newQuantity = currentQuantity - event.getQuantity();
        inventory.setQuantity(Math.max(newQuantity, 0));
        inventoryRepository.save(inventory);
    }

}

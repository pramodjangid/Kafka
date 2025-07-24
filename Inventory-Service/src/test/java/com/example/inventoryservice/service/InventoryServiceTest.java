package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
import com.example.inventoryservice.producer.InventoryServiceProducer;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.util.EventBuilderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryServiceProducer inventoryServiceProducer;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleOrderEvent_whenProductIsAvailable_shouldSendAvailableResponse() {
        OrderPlacedEvent event = new OrderPlacedEvent("order_1", "product_1", 3);
        Inventory inventory = Inventory.builder()
                .productId("product_1")
                .quantity(5)
                .build();
        when(inventoryRepository.findById("product_1")).thenReturn(Optional.of(inventory));

        inventoryService.handleOrderEvent(event);

        ArgumentCaptor<InventoryResponseEvent> captor = ArgumentCaptor.forClass(InventoryResponseEvent.class);
        verify(inventoryServiceProducer).sendInventoryResponse(captor.capture());

        InventoryResponseEvent response = captor.getValue();
        assertTrue(response.isAvailable());
        assertEquals("order_1", response.getOrderId());
    }

    @Test
    void handleOrderEvent_whenProductIsNotAvailable_shouldSendUnavailableResponse() {
        OrderPlacedEvent event = EventBuilderUtil.buildOrderPlacedEvent("order_2", "product_1", 10);
        Inventory inventory = Inventory.builder()
                .productId("product_1")
                .quantity(5)
                .build();

        when(inventoryRepository.findById("product_1")).thenReturn(Optional.of(inventory));

        inventoryService.handleOrderEvent(event);

        ArgumentCaptor<InventoryResponseEvent> captor = ArgumentCaptor.forClass(InventoryResponseEvent.class);
        verify(inventoryServiceProducer).sendInventoryResponse(captor.capture());

        InventoryResponseEvent response = captor.getValue();
        assertFalse(response.isAvailable());
        assertEquals("order_2", response.getOrderId());
    }

    @Test
    void handleOrderEvent_whenProductNotFound_shouldSendUnavailableResponse() {
        OrderPlacedEvent event = EventBuilderUtil.buildOrderPlacedEvent("order_3", "unknown_product", 1);
        when(inventoryRepository.findById("unknown_product")).thenReturn(Optional.empty());

        inventoryService.handleOrderEvent(event);

        ArgumentCaptor<InventoryResponseEvent> captor = ArgumentCaptor.forClass(InventoryResponseEvent.class);
        verify(inventoryServiceProducer).sendInventoryResponse(captor.capture());

        InventoryResponseEvent response = captor.getValue();
        assertFalse(response.isAvailable());
        assertEquals("order_3", response.getOrderId());
    }

    @Test
    void handleOrderConfirmationEvent_shouldReduceInventoryQuantity() {
        Inventory inventory = Inventory.builder()
                .productId("product_1")
                .quantity(10)
                .build();
        OrderConfirmedEvent event = EventBuilderUtil.buildOrderConfirmedEvent("order_4", "product_1", 3);

        when(inventoryRepository.findById("product_1")).thenReturn(Optional.of(inventory));

        inventoryService.handleOrderConfirmationEvent(event);

        assertEquals(7, inventory.getQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void handleOrderConfirmationEvent_shouldNotGoNegative() {
        Inventory inventory = Inventory.builder()
                .productId("product_1")
                .quantity(2)
                .build();
        OrderConfirmedEvent event = EventBuilderUtil.buildOrderConfirmedEvent("order_5", "product_1", 5);

        when(inventoryRepository.findById("product_1")).thenReturn(Optional.of(inventory));

        inventoryService.handleOrderConfirmationEvent(event);

        assertEquals(0, inventory.getQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void handleOrderConfirmationEvent_whenInventoryNotFound_shouldThrowException() {
        OrderConfirmedEvent event = EventBuilderUtil.buildOrderConfirmedEvent("order_6", "product_x", 2);

        when(inventoryRepository.findById("product_x")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                inventoryService.handleOrderConfirmationEvent(event));

        assertEquals("Inventory not found for productId: product_x", exception.getMessage());
    }
}

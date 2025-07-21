package com.example.inventoryservice.service;

import com.example.inventoryservice.model.InventoryResponseEvent;
import com.example.inventoryservice.model.OrderPlacedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryListenerTest {

    @InjectMocks
    private InventoryListener inventoryListener;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    private final String orderId = "order123";
    private final String productId = "P123";
    private final int quantity = 2;

    private String message;

    @BeforeEach
    void setup() {
        message = String.format("{\"orderId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d}", orderId, productId, quantity);
    }

    @Test
    void handleOrderEvent_productAvailable_shouldSendResponseWithAvailableTrue() throws Exception {
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(orderId, productId, quantity);
        InventoryResponseEvent expectedResponse = new InventoryResponseEvent(orderId, true, productId, quantity);

        when(objectMapper.readValue(message, OrderPlacedEvent.class)).thenReturn(orderPlacedEvent);
        when(inventoryRepository.isAvailable(productId, quantity)).thenReturn(Optional.of(true));

        inventoryListener.handleOrderEvent(message);

        verify(kafkaTemplate).send("inventory-response", orderId, expectedResponse);
    }

    @Test
    void handleOrderEvent_productNotAvailable_shouldSendResponseWithAvailableFalse() throws Exception {
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(orderId, productId, quantity);
        InventoryResponseEvent expectedResponse = new InventoryResponseEvent(orderId, false, productId, quantity);

        when(objectMapper.readValue(message, OrderPlacedEvent.class)).thenReturn(orderPlacedEvent);
        when(inventoryRepository.isAvailable(productId, quantity)).thenReturn(Optional.of(false));

        inventoryListener.handleOrderEvent(message);

        verify(kafkaTemplate).send("inventory-response", orderId, expectedResponse);
    }

    @Test
    void handleOrderEvent_duplicateOrder_shouldSkipProcessing() throws Exception {
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(orderId, productId, quantity);

        when(objectMapper.readValue(message, OrderPlacedEvent.class)).thenReturn(orderPlacedEvent);
        when(inventoryRepository.isAvailable(productId, quantity)).thenReturn(Optional.of(true));

        inventoryListener.handleOrderEvent(message);
        inventoryListener.handleOrderEvent(message);

        verify(kafkaTemplate, times(1)).send(eq("inventory-response"), eq(orderId), any(InventoryResponseEvent.class));
    }

    @Test
    void handleOrderEvent_invalidJson_shouldThrowRuntimeException() throws Exception {
        when(objectMapper.readValue(message, OrderPlacedEvent.class))
                .thenThrow(new JsonProcessingException("Malformed") {});

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryListener.handleOrderEvent(message));

        assertTrue(exception.getMessage().contains("Kafka processing failed"));
        verifyNoInteractions(kafkaTemplate);
    }
}

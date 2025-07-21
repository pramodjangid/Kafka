package com.example.inventoryservice.service;

import com.example.inventoryservice.model.OrderConfirmedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class OrderConfirmationListenerTest {

    @InjectMocks
    private OrderConfirmationListener listener;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void handleConfirmation_validMessage_shouldReduceInventory() throws Exception {
        String message = "{\"orderId\":\"order123\",\"productId\":\"P123\",\"quantity\":2}";
        OrderConfirmedEvent event = new OrderConfirmedEvent("order123", "P123", 2);

        when(objectMapper.readValue(message, OrderConfirmedEvent.class)).thenReturn(event);

        listener.handleConfirmation(message);

        verify(inventoryRepository).reduceQuantity("P123", 2);
    }

    @Test
    void handleConfirmation_invalidJson_shouldHandleExceptionGracefully() throws Exception {
        String message = "invalid-json";

        when(objectMapper.readValue(message, OrderConfirmedEvent.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        listener.handleConfirmation(message);

        verifyNoInteractions(inventoryRepository);
    }

}


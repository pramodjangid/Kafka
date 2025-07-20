package com.example.orderservice.service;

import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.service.listener.InventoryResponseListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryResponseListenerTest {

    @InjectMocks
    private InventoryResponseListener listener;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderResponseStore responseStore;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void listenInventoryResponse_productAvailable_sendsConfirmationAndCompletes() throws Exception {
        String orderId = "order123";
        String productId = "ABC123";
        int quantity = 2;

        String incomingMessage = "{\"orderId\":\"order123\",\"available\":true,\"productId\":\"ABC123\",\"quantity\":2}";
        InventoryResponseEvent event = new InventoryResponseEvent(orderId, true, productId, quantity);

        when(objectMapper.readValue(incomingMessage, InventoryResponseEvent.class)).thenReturn(event);

        listener.listenInventoryResponse(incomingMessage);

        // Verify confirmation event sent
        verify(kafkaTemplate).send(eq("order-confirmed-events"), eq(orderId),
                argThat(arg -> arg instanceof OrderConfirmedEvent &&
                        ((OrderConfirmedEvent) arg).getOrderId().equals(orderId) &&
                        ((OrderConfirmedEvent) arg).getProductId().equals(productId) &&
                        ((OrderConfirmedEvent) arg).getQuantity() == quantity));

        verify(responseStore).complete(orderId, "Order placed successfully");
    }

    @Test
    void listenInventoryResponse_productNotAvailable_completesWithFailureMessage() throws Exception {
        String orderId = "order123";
        String productId = "ABC123";
        int quantity = 2;

        String incomingMessage = "{\"orderId\":\"order123\",\"available\":false,\"productId\":\"ABC123\",\"quantity\":2}";
        InventoryResponseEvent event = new InventoryResponseEvent(orderId, false, productId, quantity);

        when(objectMapper.readValue(incomingMessage, InventoryResponseEvent.class)).thenReturn(event);

        listener.listenInventoryResponse(incomingMessage);

        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(responseStore).complete(orderId, "Product not available in inventory");
    }

    @Test
    void listenInventoryResponse_invalidJson_shouldHandleExceptionGracefully() throws Exception {
        String invalidJson = "not-a-valid-json";

        when(objectMapper.readValue(invalidJson, InventoryResponseEvent.class))
                .thenThrow(new RuntimeException("Parsing error"));

        listener.listenInventoryResponse(invalidJson);

        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(responseStore, never()).complete(any(), any());
    }

}


package com.example.orderservice.service;

import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.model.OrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderResponseStore responseStore;

    @Test
    void placeOrder_successfulResponse_returns200() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setOrderId("123");
        request.setProductId("ABC123");
        request.setQuantity(2);

        doAnswer(invocation -> {
            CompletableFuture<String> future = invocation.getArgument(1);
            future.complete("Inventory Reserved");
            return null;
        }).when(responseStore).store(anyString(), any());

        ResponseEntity<String> response = orderService.placeOrder(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Inventory Reserved", response.getBody());
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void placeOrder_timeout_returnsRequestTimeout() {
        OrderRequest request = new OrderRequest();
        request.setOrderId("123");
        request.setProductId("ABC123");
        request.setQuantity(2);

        // store a future that never completes
        doAnswer(invocation -> null).when(responseStore).store(anyString(), any());

        ResponseEntity<String> response = orderService.placeOrder(request);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertEquals("Timed out waiting for inventory response", response.getBody());
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void placeOrder_exceptionDuringProcessing_returnsInternalServerError() {
        OrderRequest request = new OrderRequest();
        request.setOrderId("123");
        request.setProductId("ABC123");
        request.setQuantity(2);

        doAnswer(invocation -> {
            CompletableFuture<String> future = invocation.getArgument(1);
            future.completeExceptionally(new RuntimeException("Simulated failure"));
            return null;
        }).when(responseStore).store(anyString(), any());

        ResponseEntity<String> response = orderService.placeOrder(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Inventory processing failed: Simulated failure", response.getBody());
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any(OrderPlacedEvent.class));
    }
}

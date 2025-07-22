package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.InventoryResponseEvent;
import com.example.orderservice.model.OrderConfirmedEvent;
import com.example.orderservice.model.OrderPlacedEvent;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.producer.OrderServiceProducer;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderServiceProducer orderServiceProducer;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void placeOrder_shouldSaveAndPublishEvent() {
        OrderRequest request = new OrderRequest();
        request.setOrderId("OD_123");
        request.setProductId("ABC");
        request.setQuantity(3);

        ResponseEntity<String> response = orderService.placeOrder(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Order placed. Check status via /orders/{orderId}/status"));
        verify(orderRepository).save(any(Order.class));
        verify(orderServiceProducer).sendOrderPlacedEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void listenInventoryResponse_shouldConfirmOrder_ifAvailable() {
        Order order = Order.builder()
                .orderId("order_123")
                .productId("product_1")
                .quantity(2)
                .status(OrderStatus.PENDING)
                .build();

        InventoryResponseEvent responseEvent = new InventoryResponseEvent("order_123", true, "product_1", 2);

        when(orderRepository.findById("order_123")).thenReturn(Optional.of(order));

        orderService.listenInventoryResponse(responseEvent);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
        verify(orderServiceProducer).sendOrderConfirmationEvent(any(OrderConfirmedEvent.class));
    }

    @Test
    void listenInventoryResponse_shouldRejectOrder_ifNotAvailable() {
        Order order = Order.builder()
                .orderId("order_123")
                .productId("product_1")
                .quantity(2)
                .status(OrderStatus.PENDING)
                .build();

        InventoryResponseEvent responseEvent = new InventoryResponseEvent("order_123", false, "product_1", 2);
        when(orderRepository.findById("order_123")).thenReturn(Optional.of(order));

        orderService.listenInventoryResponse(responseEvent);

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        verify(orderRepository).save(order);
        verify(orderServiceProducer, never()).sendOrderConfirmationEvent(any());
    }

    @Test
    void listenInventoryResponse_shouldDoNothing_ifOrderNotFound() {
        InventoryResponseEvent event = new InventoryResponseEvent("unknown", true, "product", 1);
        when(orderRepository.findById("unknown")).thenReturn(Optional.empty());

        orderService.listenInventoryResponse(event);

        verify(orderRepository, never()).save(any());
        verify(orderServiceProducer, never()).sendOrderConfirmationEvent(any());
    }

    @Test
    void listenInventoryResponse_shouldSkipAlreadyProcessedOrder() {
        Order order = Order.builder()
                .orderId("order_123")
                .productId("product_1")
                .quantity(2)
                .status(OrderStatus.CONFIRMED)
                .build();

        InventoryResponseEvent responseEvent = new InventoryResponseEvent("order_123", true, "product_1", 2);
        when(orderRepository.findById("order_123")).thenReturn(Optional.of(order));

        orderService.listenInventoryResponse(responseEvent);

        verify(orderRepository, never()).save(any());
        verify(orderServiceProducer, never()).sendOrderConfirmationEvent(any());
    }

    @Test
    void getOrderStatus_shouldReturnOk_ifOrderExists() {
        Order order = Order.builder().orderId("order_123").status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById("order_123")).thenReturn(Optional.of(order));

        ResponseEntity<String> response = orderService.getOrderStatus("order_123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order Status: CONFIRMED", response.getBody());
    }

    @Test
    void getOrderStatus_shouldReturnNotFound_ifMissing() {
        when(orderRepository.findById("order_404")).thenReturn(Optional.empty());

        ResponseEntity<String> response = orderService.getOrderStatus("order_404");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Order not found with ID: order_404", response.getBody());
    }
}

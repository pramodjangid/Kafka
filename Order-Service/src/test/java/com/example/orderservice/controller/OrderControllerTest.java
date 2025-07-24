package com.example.orderservice.controller;

import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @Test
    void placeOrder_delegatesToService() {
        OrderRequest request = new OrderRequest();
        ResponseEntity<String> mockedResponse = ResponseEntity.ok("Mocked");
        when(orderService.placeOrder(any())).thenReturn(mockedResponse);

        ResponseEntity<String> response = orderController.placeOrder(request);

        assertEquals("Mocked", response.getBody());
        verify(orderService).placeOrder(request);
    }
}

package com.example.orderservice.entity;

import com.example.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    private String orderId;
    private String productId;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}

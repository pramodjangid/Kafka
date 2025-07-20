package com.example.orderservice.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String orderId;
    @NotBlank
    private String productId;
    @Min(1)
    private int quantity;
}

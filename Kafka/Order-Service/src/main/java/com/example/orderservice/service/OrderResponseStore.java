package com.example.orderservice.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderResponseStore {
    private final Map<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

    public void store(String orderId, CompletableFuture<String> future) {
        futures.put(orderId, future);
    }

    public void complete(String orderId, String response) {
        CompletableFuture<String> future = futures.remove(orderId);
        if (future != null) {
            future.complete(response);
        }
    }

    public void completeExceptionally(String orderId, Throwable throwable) {
        CompletableFuture<String> future = futures.remove(orderId);
        if (future != null) {
            future.completeExceptionally(throwable);
        }
    }

}

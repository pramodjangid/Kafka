package com.example.orderservice.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class OrderResponseStoreTest {

    private final OrderResponseStore store = new OrderResponseStore();

    @Test
    void storeAndComplete_shouldResolveFutureWithGivenResponse() throws Exception {
        String orderId = "order123";
        CompletableFuture<String> future = new CompletableFuture<>();

        store.store(orderId, future);
        store.complete(orderId, "Success");

        assertEquals("Success", future.get());
    }

    @Test
    void complete_withUnknownOrderId_shouldNotThrowException() {
        assertDoesNotThrow(() -> store.complete("unknownOrderId", "Anything"));
    }

    @Test
    void complete_shouldRemoveTheFutureFromMap() throws Exception {
        String orderId = "order456";
        CompletableFuture<String> future = new CompletableFuture<>();

        store.store(orderId, future);
        store.complete(orderId, "Done");

        store.complete(orderId, "Ignored");

        assertEquals("Done", future.get());
    }

    @Test
    void store_shouldOverwriteExistingFuture() throws Exception {
        String orderId = "order789";

        CompletableFuture<String> originalFuture = new CompletableFuture<>();
        CompletableFuture<String> newFuture = new CompletableFuture<>();

        store.store(orderId, originalFuture);
        store.store(orderId, newFuture);

        store.complete(orderId, "Final");

        assertFalse(originalFuture.isDone());
        assertEquals("Final", newFuture.get());
    }

}


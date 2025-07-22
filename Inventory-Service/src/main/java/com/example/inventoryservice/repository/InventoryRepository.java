package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
}

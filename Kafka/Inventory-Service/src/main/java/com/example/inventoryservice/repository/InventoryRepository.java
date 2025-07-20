package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.Inventory;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    @Query("SELECT CASE WHEN i.quantity >= :quantity THEN true ELSE false END FROM Inventory i WHERE i.productId = :productId")
    Optional<Boolean> isAvailable(@Param("productId") String productId, @Param("quantity") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.productId = :productId")
    void reduceQuantity(@Param("productId") String productId, @Param("quantity") int quantity);
}

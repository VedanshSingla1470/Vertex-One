package com.vertexone.prod.inventory.balance.repository;

import com.vertexone.prod.inventory.balance.entity.InventoryBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryBalanceRepository
        extends JpaRepository<InventoryBalance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM InventoryBalance b
        WHERE b.organization.id = :organizationId
          AND b.warehouse.id = :warehouseId
          AND b.product.id = :productId
    """)
    Optional<InventoryBalance> findForUpdate(
            @Param("organizationId") UUID organizationId,
            @Param("warehouseId") UUID warehouseId,
            @Param("productId") UUID productId
    );

    Page<InventoryBalance> findByOrganizationIdAndWarehouseId(
            UUID organizationId, UUID warehouseId, Pageable pageable
    );

    @Query("""
        SELECT b FROM InventoryBalance b
        WHERE b.organization.id = :organizationId
          AND b.warehouse.id = :warehouseId
          AND b.quantityOnHand <= b.reorderLevel
    """)
    Page<InventoryBalance> findLowStockByWarehouse(
            @Param("organizationId") UUID organizationId,
            @Param("warehouseId") UUID warehouseId,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM InventoryBalance b
        WHERE b.organization.id = :organizationId
          AND b.quantityOnHand <= b.reorderLevel
    """)
    Page<InventoryBalance> findLowStock(
            @Param("organizationId") UUID organizationId,
            Pageable pageable
    );
}
package com.vertexone.prod.inventory.movement.repository;

import com.vertexone.prod.inventory.movement.entity.StockMovement;
import com.vertexone.prod.inventory.movement.enums.StockMovementType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByOrganizationId(
            UUID organizationId, Pageable pageable
    );

    Page<StockMovement> findByOrganizationIdAndProductId(
            UUID organizationId, UUID productId, Pageable pageable
    );

    Page<StockMovement> findByOrganizationIdAndMovementType(
            UUID organizationId, StockMovementType movementType, Pageable pageable
    );

    Page<StockMovement> findByOrganizationIdAndSourceWarehouseIdOrOrganizationIdAndDestinationWarehouseId(
            UUID organizationId1, UUID sourceWarehouseId,
            UUID organizationId2, UUID destinationWarehouseId,
            Pageable pageable
    );
}
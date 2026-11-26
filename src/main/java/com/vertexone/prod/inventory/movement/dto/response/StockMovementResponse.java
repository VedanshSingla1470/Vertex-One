package com.vertexone.prod.inventory.movement.dto.response;

import com.vertexone.prod.inventory.movement.enums.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String productName,
        StockMovementType movementType,
        UUID sourceWarehouseId,
        String sourceWarehouseName,
        UUID destinationWarehouseId,
        String destinationWarehouseName,
        BigDecimal quantity,
        String referenceNumber,
        String reason,
        UUID performedByUserId,
        Instant createdAt
) {}
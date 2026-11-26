package com.vertexone.prod.inventory.balance.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryBalanceResponse(
        UUID id,
        UUID warehouseId,
        String warehouseName,
        UUID productId,
        String productName,
        String sku,
        BigDecimal quantityOnHand,
        BigDecimal reorderLevel,
        boolean lowStock,
        Instant updatedAt
) {}
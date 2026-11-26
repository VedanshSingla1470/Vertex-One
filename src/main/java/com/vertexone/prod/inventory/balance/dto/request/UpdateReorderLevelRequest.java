package com.vertexone.prod.inventory.balance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateReorderLevelRequest(
        @NotNull UUID productId,
        @NotNull UUID warehouseId,
        @NotNull @DecimalMin("0.0000") BigDecimal reorderLevel
) {}
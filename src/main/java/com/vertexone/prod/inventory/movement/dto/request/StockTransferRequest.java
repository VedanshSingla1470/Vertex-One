package com.vertexone.prod.inventory.movement.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record StockTransferRequest(
        @NotNull UUID productId,
        @NotNull UUID sourceWarehouseId,
        @NotNull UUID destinationWarehouseId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantity,
        @Size(max = 100) String referenceNumber,
        @NotBlank @Size(max = 500) String reason
) {}
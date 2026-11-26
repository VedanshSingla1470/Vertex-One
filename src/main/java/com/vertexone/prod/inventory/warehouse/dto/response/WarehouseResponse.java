package com.vertexone.prod.inventory.warehouse.dto.response;

import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import java.time.Instant;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        UUID organizationId,
        String name,
        String code,
        String description,
        String address,
        WarehouseStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
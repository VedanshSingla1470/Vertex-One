package com.vertexone.prod.product.dto.response;

import com.vertexone.prod.product.enums.ProductStatus;
import com.vertexone.prod.product.enums.UnitOfMeasure;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(

        UUID id,
        UUID organizationId,
        UUID categoryId,
        String sku,
        String name,
        String description,
        UnitOfMeasure unitOfMeasure,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt

) {}

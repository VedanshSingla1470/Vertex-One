package com.vertexone.prod.product.category.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProductCategoryResponse(

        UUID id,
        UUID organizationId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt

) {}

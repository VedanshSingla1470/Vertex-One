package com.vertexone.prod.vendor.category.dto.response;

import java.time.Instant;
import java.util.UUID;

public record VendorCategoryResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
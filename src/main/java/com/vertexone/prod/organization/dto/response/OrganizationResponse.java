
package com.vertexone.prod.organization.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Instant createdAt
) {}

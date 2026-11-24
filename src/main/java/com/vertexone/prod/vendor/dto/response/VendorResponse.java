
package com.vertexone.prod.vendor.dto.response;

import com.vertexone.prod.vendor.enums.VendorStatus;

import java.time.Instant;
import java.util.UUID;

public record VendorResponse(
        UUID id,
        UUID organizationId,
        String name,
        String email,
        String phone,
        String website,
        String address,
        String taxId,
        VendorStatus status,
        UUID categoryId,
        Instant createdAt,
        Instant updatedAt
) {}

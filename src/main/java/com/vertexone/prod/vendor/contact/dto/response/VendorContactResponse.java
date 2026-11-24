package com.vertexone.prod.vendor.contact.dto.response;

import java.time.Instant;
import java.util.UUID;

public record VendorContactResponse(
        UUID id,
        UUID vendorId,
        String name,
        String email,
        String phone,
        String designation,
        boolean primary,
        Instant createdAt,
        Instant updatedAt
) {}
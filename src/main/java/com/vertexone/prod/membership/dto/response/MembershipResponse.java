
package com.vertexone.prod.membership.dto.response;

import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.enums.MembershipStatus;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(
        UUID id,
        UUID userId,
        String userEmail,
        UUID organizationId,
        String organizationName,
        MembershipRole role,
        MembershipStatus status,
        Instant createdAt
) {}

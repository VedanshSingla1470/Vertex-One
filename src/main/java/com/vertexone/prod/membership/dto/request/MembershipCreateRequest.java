
package com.vertexone.prod.membership.dto.request;

import com.vertexone.prod.membership.enums.MembershipRole;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MembershipCreateRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Organization ID is required")
        UUID organizationId,

        @NotNull(message = "Membership role is required")
        MembershipRole role

) {}

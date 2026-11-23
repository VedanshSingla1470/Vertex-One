package com.vertexone.prod.membership.dto.request;

import com.vertexone.prod.membership.enums.MembershipRole;
import jakarta.validation.constraints.NotNull;

public record MembershipRoleUpdateRequest(

        @NotNull(message = "Role is required")
        MembershipRole role

) {}

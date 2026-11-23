package com.vertexone.prod.membership.dto.request;

import com.vertexone.prod.membership.enums.MembershipStatus;
import jakarta.validation.constraints.NotNull;

public record MembershipStatusUpdateRequest(

        @NotNull(message = "Membership status is required")
        MembershipStatus status

) {}

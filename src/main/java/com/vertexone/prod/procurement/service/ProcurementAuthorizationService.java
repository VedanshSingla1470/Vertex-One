package com.vertexone.prod.procurement.service;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcurementAuthorizationService {

    private final OrganizationAuthorizationService organizationAuthorizationService;

    public ProcurementAuthorizationService(
            OrganizationAuthorizationService organizationAuthorizationService
    ) {
        this.organizationAuthorizationService = organizationAuthorizationService;
    }

    public Membership requireActiveMembership(
            String actorEmail,
            UUID organizationId
    ) {
        return organizationAuthorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );
    }

    public Membership requireProcurementAccess(
            String actorEmail,
            UUID organizationId
    ) {
        Membership membership = requireActiveMembership(
                actorEmail,
                organizationId
        );

        MembershipRole role = membership.getRole();

        if (role != MembershipRole.OWNER
                && role != MembershipRole.ADMIN
                && role != MembershipRole.PROCUREMENT_MANAGER) {
            throw new IllegalStateException(
                    "You do not have permission to manage procurement."
            );
        }

        return membership;
    }

    public Membership requireApprovalAccess(
            String actorEmail,
            UUID organizationId
    ) {
        return requireProcurementAccess(actorEmail, organizationId);
    }
}
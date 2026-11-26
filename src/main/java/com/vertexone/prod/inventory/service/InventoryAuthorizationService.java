package com.vertexone.prod.inventory.service;

import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class InventoryAuthorizationService {

    private final OrganizationAuthorizationService organizationAuthorizationService;

    private static final Set<MembershipRole> INVENTORY_MANAGERS = Set.of(
            MembershipRole.OWNER,
            MembershipRole.ADMIN,
            MembershipRole.INVENTORY_MANAGER
    );

    public InventoryAuthorizationService(
            OrganizationAuthorizationService organizationAuthorizationService) {
        this.organizationAuthorizationService = organizationAuthorizationService;
    }

    public void requireViewAccess(String actorEmail, UUID organizationId) {
        organizationAuthorizationService.requireActiveMembership(
                actorEmail, organizationId
        );
    }

    public void requireInventoryManagement(
            String actorEmail, UUID organizationId) {

        var membership = organizationAuthorizationService.requireActiveMembership(
                actorEmail, organizationId
        );

        if (!INVENTORY_MANAGERS.contains(membership.getRole())) {
            throw new IllegalStateException(
                    "You do not have permission to manage inventory."
            );
        }
    }
}
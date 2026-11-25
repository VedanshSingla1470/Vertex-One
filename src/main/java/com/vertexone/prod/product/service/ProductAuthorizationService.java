package com.vertexone.prod.product.service;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductAuthorizationService {

    private final OrganizationAuthorizationService authorizationService;

    public Membership requireActiveMembership(
            String actorEmail,
            UUID organizationId
    ) {
        return authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );
    }

    public Membership requireCatalogManager(
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

            throw new AccessDeniedException(
                    "You do not have permission to manage the product catalog"
            );
        }

        return membership;
    }
}

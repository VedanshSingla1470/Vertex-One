
package com.vertexone.prod.membership.service;

import com.vertexone.prod.common.exception.ResourceNotFoundException;
import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.enums.MembershipStatus;
import com.vertexone.prod.membership.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationAuthorizationService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public Membership requireActiveMembership(
            String actorEmail,
            UUID organizationId) {

        User actor = userRepository.findByEmail(
                actorEmail.trim().toLowerCase()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Authenticated user not found"
        ));

        Membership membership = membershipRepository
                .findByUserIdAndOrganizationId(
                        actor.getId(),
                        organizationId
                )
                .orElseThrow(() -> new AccessDeniedException(
                        "You do not have access to this organization"
                ));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new AccessDeniedException(
                    "Your organization membership is not active"
            );
        }

        return membership;
    }

    public Membership requireOwnerOrAdmin(
            String actorEmail,
            UUID organizationId) {

        Membership membership = requireActiveMembership(
                actorEmail,
                organizationId
        );

        MembershipRole role = membership.getRole();

        if (role != MembershipRole.OWNER
                && role != MembershipRole.ADMIN) {
            throw new AccessDeniedException(
                    "Only organization owners and admins can perform this action"
            );
        }

        return membership;
    }

    public Membership requireOwner(
            String actorEmail,
            UUID organizationId) {

        Membership membership = requireActiveMembership(
                actorEmail,
                organizationId
        );

        if (membership.getRole() != MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "Only the organization owner can perform this action"
            );
        }

        return membership;
    }
}

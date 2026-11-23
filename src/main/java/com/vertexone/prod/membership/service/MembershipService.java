
package com.vertexone.prod.membership.service;

import com.vertexone.prod.common.exception.DuplicateResourceException;
import com.vertexone.prod.common.exception.ResourceNotFoundException;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;

import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;

import com.vertexone.prod.membership.dto.request.MembershipCreateRequest;
import com.vertexone.prod.membership.dto.response.MembershipResponse;
import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.enums.MembershipStatus;
import com.vertexone.prod.membership.repository.MembershipRepository;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import com.vertexone.prod.membership.dto.request.MembershipRoleUpdateRequest;
import com.vertexone.prod.membership.dto.request.MembershipStatusUpdateRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;

    public MembershipResponse createMembership(
            MembershipCreateRequest request,
            String actorEmail) {

        User actor = userRepository.findByEmail(
                actorEmail.trim().toLowerCase()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Authenticated user not found"
        ));

        Organization organization = organizationRepository
                .findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found"
                ));

        Membership actorMembership =
                authorizationService.requireOwnerOrAdmin(
                        actorEmail,
                        organization.getId()
                );

        MembershipRole actorRole = actorMembership.getRole();
        MembershipRole requestedRole = request.role();

        // Only the organization creation flow can assign OWNER.
        if (requestedRole == MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "OWNER role is assigned automatically during organization creation"
            );
        }

        if (actorRole != MembershipRole.OWNER
                && actorRole != MembershipRole.ADMIN) {
            throw new AccessDeniedException(
                    "You do not have permission to add organization members"
            );
        }

        // ADMIN cannot promote another member to ADMIN.
        if (actorRole == MembershipRole.ADMIN
                && requestedRole == MembershipRole.ADMIN) {
            throw new AccessDeniedException(
                    "Only the organization owner can assign the ADMIN role"
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        Membership existingMembership = membershipRepository
                .findByUserIdAndOrganizationId(
                        user.getId(),
                        organization.getId()
                )
                .orElse(null);

        if (existingMembership != null) {

            if (existingMembership.getStatus() == MembershipStatus.REMOVED) {

                existingMembership.setRole(requestedRole);
                existingMembership.setStatus(MembershipStatus.ACTIVE);
                existingMembership.setRemovedAt(null);
                existingMembership.setRemovedBy(null);

                Membership reactivated =
                        membershipRepository.save(existingMembership);

                return toResponse(reactivated);
            }

            throw new DuplicateResourceException(
                    "User already belongs to this organization"
            );
        }

        Membership membership = Membership.builder()
                .user(user)
                .organization(organization)
                .role(requestedRole)
                .status(MembershipStatus.ACTIVE)
                .build();

        Membership saved = membershipRepository.save(membership);

        return toResponse(saved);
    }


    public List<MembershipResponse> getOrganizationMembers(
            UUID organizationId,
            String actorEmail) {

        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        return membershipRepository.findByOrganizationId(organizationId)
                .stream()
                .filter(membership ->
                        membership.getStatus() != MembershipStatus.REMOVED)
                .map(this::toResponse)
                .toList();
    }

    public MembershipResponse updateMemberRole(
            UUID organizationId,
            UUID membershipId,
            MembershipRoleUpdateRequest request,
            String actorEmail) {

        authorizationService.requireOwner(
                actorEmail,
                organizationId
        );

        if (request.role() == MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "OWNER role cannot be assigned through membership management"
            );
        }

        Membership target = getOrganizationMembership(
                organizationId,
                membershipId
        );

        if (target.getRole() == MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "The organization owner's role cannot be changed here"
            );
        }

        target.setRole(request.role());

        return toResponse(membershipRepository.save(target));
    }

    public MembershipResponse updateMemberStatus(
            UUID organizationId,
            UUID membershipId,
            MembershipStatusUpdateRequest request,
            String actorEmail) {

        Membership actor = authorizationService.requireOwnerOrAdmin(
                actorEmail,
                organizationId
        );

        MembershipStatus requestedStatus = request.status();

        if (requestedStatus != MembershipStatus.ACTIVE
                && requestedStatus != MembershipStatus.SUSPENDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ACTIVE and SUSPENDED statuses can be assigned here"
            );
        }

        Membership target = getOrganizationMembership(
                organizationId,
                membershipId
        );

        if (target.getRole() == MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "The organization owner cannot be suspended"
            );
        }

        if (actor.getRole() == MembershipRole.ADMIN
                && target.getRole() == MembershipRole.ADMIN) {
            throw new AccessDeniedException(
                    "Admins cannot modify other admins"
            );
        }

        if (actor.getId().equals(target.getUser().getId())
                && requestedStatus == MembershipStatus.SUSPENDED) {
            throw new AccessDeniedException(
                    "You cannot suspend your own membership"
            );
        }

        target.setStatus(requestedStatus);

        return toResponse(membershipRepository.save(target));
    }

    public void removeMember(
            UUID organizationId,
            UUID membershipId,
            String actorEmail) {

        Membership actor = authorizationService.requireOwnerOrAdmin(
                actorEmail,
                organizationId
        );

        Membership target = getOrganizationMembership(
                organizationId,
                membershipId
        );

        if (target.getRole() == MembershipRole.OWNER) {
            throw new AccessDeniedException(
                    "The organization owner cannot be removed"
            );
        }

        if (actor.getRole() == MembershipRole.ADMIN
                && target.getRole() == MembershipRole.ADMIN) {
            throw new AccessDeniedException(
                    "Admins cannot remove other admins"
            );
        }

        if (actor.getId().equals(target.getUser().getId())) {
            throw new AccessDeniedException(
                    "You cannot remove your own membership through this endpoint"
            );
        }

        User removedBy = userRepository.findByEmail(
                actorEmail.trim().toLowerCase()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Authenticated user not found"
        ));

        target.setStatus(MembershipStatus.REMOVED);
        target.setRemovedAt(Instant.now());
        target.setRemovedBy(removedBy);

        membershipRepository.save(target);
    }

    private Membership getOrganizationMembership(
            UUID organizationId,
            UUID membershipId) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership not found"
                ));

        if (!membership.getOrganization().getId().equals(organizationId)) {
            throw new ResourceNotFoundException(
                    "Membership not found in this organization"
            );
        }

        return membership;
    }

    private MembershipResponse toResponse(Membership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getUser().getId(),
                membership.getUser().getEmail(),
                membership.getOrganization().getId(),
                membership.getOrganization().getName(),
                membership.getRole(),
                membership.getStatus(),
                membership.getCreatedAt()
        );
    }
}

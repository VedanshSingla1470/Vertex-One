
package com.vertexone.prod.organization.service;

import com.vertexone.prod.common.exception.DuplicateResourceException;
import com.vertexone.prod.common.exception.ResourceNotFoundException;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.enums.MembershipStatus;
import com.vertexone.prod.membership.repository.MembershipRepository;

import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import com.vertexone.prod.organization.dto.request.OrganizationCreateRequest;
import com.vertexone.prod.organization.dto.response.OrganizationResponse;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationAuthorizationService authorizationService;

    public OrganizationResponse createOrganization(
            OrganizationCreateRequest request,
            String creatorEmail) {

        String slug = request.slug().trim().toLowerCase();

        if (organizationRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException(
                    "An organization with this slug already exists"
            );
        }

        User creator = userRepository.findByEmail(
                creatorEmail.trim().toLowerCase()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Creator account not found"
        ));

        Organization organization = Organization.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .build();

        Organization saved = organizationRepository.save(organization);

        // Automatically make the creator the organization owner.
        Membership ownerMembership = Membership.builder()
                .user(creator)
                .organization(saved)
                .role(MembershipRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();

        membershipRepository.save(ownerMembership);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(
            String slug,
            String actorEmail) {

        Organization organization = organizationRepository
                .findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found"
                ));

        authorizationService.requireActiveMembership(
                actorEmail,
                organization.getId()
        );

        return toResponse(organization);
    }

    private OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getDescription(),
                organization.getCreatedAt()
        );
    }
}

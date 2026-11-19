
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
import com.vertexone.prod.membership.enums.MembershipStatus;
import com.vertexone.prod.membership.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public MembershipResponse createMembership(
            MembershipCreateRequest request) {

        if (membershipRepository.existsByUserIdAndOrganizationId(
                request.userId(), request.organizationId())) {

            throw new DuplicateResourceException(
                    "User already belongs to this organization"
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        Organization organization = organizationRepository
                .findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found"
                ));

        Membership membership = Membership.builder()
                .user(user)
                .organization(organization)
                .role(request.role())
                .status(MembershipStatus.ACTIVE)
                .build();

        Membership saved = membershipRepository.save(membership);

        return toResponse(saved);
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

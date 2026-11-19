
package com.vertexone.prod.organization.service;

import com.vertexone.prod.common.exception.DuplicateResourceException;
import com.vertexone.prod.common.exception.ResourceNotFoundException;
import com.vertexone.prod.organization.dto.request.OrganizationCreateRequest;
import com.vertexone.prod.organization.dto.response.OrganizationResponse;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationResponse createOrganization(
            OrganizationCreateRequest request) {

        String slug = request.slug().trim().toLowerCase();

        if (organizationRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException(
                    "An organization with this slug already exists"
            );
        }

        Organization organization = Organization.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .build();

        Organization saved = organizationRepository.save(organization);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug) {

        Organization organization = organizationRepository
                .findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found"
                ));

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

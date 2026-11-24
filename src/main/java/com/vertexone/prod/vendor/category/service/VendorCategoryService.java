package com.vertexone.prod.vendor.category.service;

import com.vertexone.prod.common.exception.DuplicateResourceException;
import com.vertexone.prod.common.exception.ResourceNotFoundException;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;

import com.vertexone.prod.vendor.category.dto.request.VendorCategoryCreateRequest;
import com.vertexone.prod.vendor.category.dto.response.VendorCategoryResponse;
import com.vertexone.prod.vendor.category.entity.VendorCategory;
import com.vertexone.prod.vendor.category.repository.VendorCategoryRepository;
import com.vertexone.prod.vendor.repository.VendorRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorCategoryService {

    private final VendorCategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;

    public VendorCategoryResponse createCategory(
            UUID organizationId,
            VendorCategoryCreateRequest request,
            String actorEmail) {

        requireManager(actorEmail, organizationId);

        String name = request.name().trim();

        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId, name)) {
            throw new DuplicateResourceException(
                    "This vendor category already exists"
            );
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Organization not found"));

        VendorCategory category = VendorCategory.builder()
                .organization(organization)
                .name(name)
                .description(normalize(request.description()))
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<VendorCategoryResponse> getCategories(
            UUID organizationId,
            String actorEmail) {

        authorizationService.requireActiveMembership(actorEmail, organizationId);

        return categoryRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VendorCategoryResponse updateCategory(
            UUID organizationId,
            UUID categoryId,
            VendorCategoryCreateRequest request,
            String actorEmail) {

        requireManager(actorEmail, organizationId);

        VendorCategory category = getCategory(organizationId, categoryId);
        String name = request.name().trim();

        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
                organizationId, name, categoryId)) {
            throw new DuplicateResourceException(
                    "This vendor category already exists"
            );
        }

        category.setName(name);
        category.setDescription(normalize(request.description()));

        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(
            UUID organizationId,
            UUID categoryId,
            String actorEmail) {

        requireManager(actorEmail, organizationId);

        VendorCategory category = getCategory(organizationId, categoryId);

        if (vendorRepository.existsByOrganizationIdAndCategoryId(
                organizationId, categoryId)) {
            throw new IllegalStateException(
                    "Remove this category from its vendors before deleting it"
            );
        }

        categoryRepository.delete(category);
    }

    private VendorCategory getCategory(UUID organizationId, UUID categoryId) {
        return categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor category not found"));
    }

    private void requireManager(String email, UUID organizationId) {
        var membership =
                authorizationService.requireActiveMembership(email, organizationId);

        var role = membership.getRole();

        if (role != com.vertexone.prod.membership.enums.MembershipRole.OWNER
                && role != com.vertexone.prod.membership.enums.MembershipRole.ADMIN
                && role != com.vertexone.prod.membership.enums.MembershipRole.PROCUREMENT_MANAGER) {
            throw new AccessDeniedException(
                    "You do not have permission to manage vendor categories"
            );
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private VendorCategoryResponse toResponse(VendorCategory category) {
        return new VendorCategoryResponse(
                category.getId(),
                category.getOrganization().getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
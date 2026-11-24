
package com.vertexone.prod.vendor.service;

import com.vertexone.prod.common.exception.DuplicateResourceException;
import com.vertexone.prod.common.exception.ResourceNotFoundException;
import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;
import com.vertexone.prod.vendor.dto.request.VendorCreateRequest;
import com.vertexone.prod.vendor.dto.request.VendorUpdateRequest;
import com.vertexone.prod.vendor.dto.response.VendorResponse;
import com.vertexone.prod.vendor.entity.Vendor;
import com.vertexone.prod.vendor.enums.VendorStatus;
import com.vertexone.prod.vendor.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService authorizationService;

    public VendorResponse createVendor(
            UUID organizationId,
            VendorCreateRequest request,
            String actorEmail) {

        requireVendorManager(actorEmail, organizationId);

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found"
                ));

        String taxId = normalize(request.taxId());

        validateTaxId(organizationId, taxId);

        Vendor vendor = Vendor.builder()
                .organization(organization)
                .name(request.name().trim())
                .email(normalize(request.email()))
                .phone(normalize(request.phone()))
                .website(normalize(request.website()))
                .address(normalize(request.address()))
                .taxId(taxId)
                .status(VendorStatus.ACTIVE)
                .build();

        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> getOrganizationVendors(
            UUID organizationId,
            String actorEmail) {

        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        return vendorRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(
            UUID organizationId,
            UUID vendorId,
            String actorEmail) {

        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        Vendor vendor = getOrganizationVendor(organizationId, vendorId);

        return toResponse(vendor);
    }

    public VendorResponse updateVendor(
            UUID organizationId,
            UUID vendorId,
            VendorUpdateRequest request,
            String actorEmail) {

        requireVendorManager(actorEmail, organizationId);

        Vendor vendor = getOrganizationVendor(organizationId, vendorId);

        String taxId = normalize(request.taxId());

        validateTaxId(organizationId, taxId, vendorId);

        vendor.setName(request.name().trim());
        vendor.setEmail(normalize(request.email()));
        vendor.setPhone(normalize(request.phone()));
        vendor.setWebsite(normalize(request.website()));
        vendor.setAddress(normalize(request.address()));
        vendor.setTaxId(taxId);

        return toResponse(vendorRepository.save(vendor));
    }

    public VendorResponse updateVendorStatus(
            UUID organizationId,
            UUID vendorId,
            VendorStatus status,
            String actorEmail) {

        requireVendorManager(actorEmail, organizationId);

        Vendor vendor = getOrganizationVendor(organizationId, vendorId);

        vendor.setStatus(status);

        return toResponse(vendorRepository.save(vendor));
    }

    private void requireVendorManager(
            String actorEmail,
            UUID organizationId) {

        Membership membership =
                authorizationService.requireActiveMembership(
                        actorEmail,
                        organizationId
                );

        MembershipRole role = membership.getRole();

        if (role != MembershipRole.OWNER
                && role != MembershipRole.ADMIN
                && role != MembershipRole.PROCUREMENT_MANAGER) {

            throw new AccessDeniedException(
                    "You do not have permission to manage vendors"
            );
        }
    }

    private Vendor getOrganizationVendor(
            UUID organizationId,
            UUID vendorId) {

        return vendorRepository.findByIdAndOrganizationId(
                vendorId,
                organizationId
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Vendor not found in this organization"
        ));
    }

    private void validateTaxId(
            UUID organizationId,
            String taxId) {

        if (taxId != null
                && vendorRepository.existsByOrganizationIdAndTaxId(
                organizationId,
                taxId
        )) {

            throw new DuplicateResourceException(
                    "A vendor with this tax ID already exists"
            );
        }
    }

    private void validateTaxId(
            UUID organizationId,
            String taxId,
            UUID vendorId) {

        if (taxId != null
                && vendorRepository.existsByOrganizationIdAndTaxIdAndIdNot(
                organizationId,
                taxId,
                vendorId
        )) {

            throw new DuplicateResourceException(
                    "A vendor with this tax ID already exists"
            );
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private VendorResponse toResponse(Vendor vendor) {

        return new VendorResponse(
                vendor.getId(),
                vendor.getOrganization().getId(),
                vendor.getName(),
                vendor.getEmail(),
                vendor.getPhone(),
                vendor.getWebsite(),
                vendor.getAddress(),
                vendor.getTaxId(),
                vendor.getStatus(),
                vendor.getCategory() != null
                        ? vendor.getCategory().getId()
                        : null,
                vendor.getCreatedAt(),
                vendor.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<VendorResponse> searchVendors(
            UUID organizationId,
            String keyword,
            VendorStatus status,
            Pageable pageable,
            String actorEmail) {

        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        String normalizedKeyword = normalize(keyword);

        return vendorRepository.searchVendors(
                organizationId,
                normalizedKeyword,
                status,
                pageable
        ).map(this::toResponse);
    }

}

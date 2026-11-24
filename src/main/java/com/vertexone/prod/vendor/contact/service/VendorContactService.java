package com.vertexone.prod.vendor.contact.service;

import com.vertexone.prod.common.exception.ResourceNotFoundException;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;

import com.vertexone.prod.vendor.contact.dto.request.VendorContactRequest;
import com.vertexone.prod.vendor.contact.dto.response.VendorContactResponse;
import com.vertexone.prod.vendor.contact.entity.VendorContact;
import com.vertexone.prod.vendor.contact.repository.VendorContactRepository;
import com.vertexone.prod.vendor.entity.Vendor;
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
public class VendorContactService {

    private final VendorContactRepository contactRepository;
    private final VendorRepository vendorRepository;
    private final OrganizationAuthorizationService authorizationService;

    public VendorContactResponse addContact(
            UUID organizationId,
            UUID vendorId,
            VendorContactRequest request,
            String actorEmail) {

        requireManager(actorEmail, organizationId);

        Vendor vendor = getVendor(organizationId, vendorId);

        if (request.primary()) {
            contactRepository.findByVendorIdAndPrimaryTrue(vendorId)
                    .ifPresent(existing -> existing.setPrimary(false));
        }

        VendorContact contact = VendorContact.builder()
                .vendor(vendor)
                .name(request.name().trim())
                .email(normalize(request.email()))
                .phone(normalize(request.phone()))
                .designation(normalize(request.designation()))
                .primary(request.primary())
                .build();

        return toResponse(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public List<VendorContactResponse> getContacts(
            UUID organizationId,
            UUID vendorId,
            String actorEmail) {

        authorizationService.requireActiveMembership(actorEmail, organizationId);
        getVendor(organizationId, vendorId);

        return contactRepository.findByVendorId(vendorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VendorContactResponse updateContact(
            UUID organizationId,
            UUID vendorId,
            UUID contactId,
            VendorContactRequest request,
            String actorEmail) {

        requireManager(actorEmail, organizationId);
        getVendor(organizationId, vendorId);

        VendorContact contact = contactRepository
                .findByIdAndVendorId(contactId, vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor contact not found"));

        if (request.primary()) {
            contactRepository.findByVendorIdAndPrimaryTrue(vendorId)
                    .filter(existing -> !existing.getId().equals(contactId))
                    .ifPresent(existing -> existing.setPrimary(false));
        }

        contact.setName(request.name().trim());
        contact.setEmail(normalize(request.email()));
        contact.setPhone(normalize(request.phone()));
        contact.setDesignation(normalize(request.designation()));
        contact.setPrimary(request.primary());

        return toResponse(contactRepository.save(contact));
    }

    public void deleteContact(
            UUID organizationId,
            UUID vendorId,
            UUID contactId,
            String actorEmail) {

        requireManager(actorEmail, organizationId);
        getVendor(organizationId, vendorId);

        VendorContact contact = contactRepository
                .findByIdAndVendorId(contactId, vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor contact not found"));

        contactRepository.delete(contact);
    }

    private Vendor getVendor(UUID organizationId, UUID vendorId) {
        return vendorRepository.findByIdAndOrganizationId(vendorId, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));
    }

    private void requireManager(String email, UUID organizationId) {
        var membership =
                authorizationService.requireActiveMembership(email, organizationId);

        MembershipRole role = membership.getRole();

        if (role != MembershipRole.OWNER
                && role != MembershipRole.ADMIN
                && role != MembershipRole.PROCUREMENT_MANAGER) {
            throw new AccessDeniedException(
                    "You do not have permission to manage vendor contacts"
            );
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private VendorContactResponse toResponse(VendorContact contact) {
        return new VendorContactResponse(
                contact.getId(),
                contact.getVendor().getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getDesignation(),
                contact.isPrimary(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}
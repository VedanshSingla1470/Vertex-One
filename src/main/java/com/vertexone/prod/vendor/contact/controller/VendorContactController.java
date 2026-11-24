package com.vertexone.prod.vendor.contact.controller;

import com.vertexone.prod.vendor.contact.dto.request.VendorContactRequest;
import com.vertexone.prod.vendor.contact.dto.response.VendorContactResponse;
import com.vertexone.prod.vendor.contact.service.VendorContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/v1/organizations/{organizationId}/vendors/{vendorId}/contacts"
)
public class VendorContactController {

    private final VendorContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorContactResponse add(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorContactRequest request,
            Authentication authentication) {

        return contactService.addContact(
                organizationId, vendorId, request, authentication.getName());
    }

    @GetMapping
    public List<VendorContactResponse> getAll(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            Authentication authentication) {

        return contactService.getContacts(
                organizationId, vendorId, authentication.getName());
    }

    @PutMapping("/{contactId}")
    public VendorContactResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            @PathVariable UUID contactId,
            @Valid @RequestBody VendorContactRequest request,
            Authentication authentication) {

        return contactService.updateContact(
                organizationId, vendorId, contactId,
                request, authentication.getName());
    }

    @DeleteMapping("/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            @PathVariable UUID contactId,
            Authentication authentication) {

        contactService.deleteContact(
                organizationId, vendorId, contactId, authentication.getName());
    }
}
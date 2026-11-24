
package com.vertexone.prod.vendor.controller;

import com.vertexone.prod.vendor.dto.request.VendorCreateRequest;
import com.vertexone.prod.vendor.dto.request.VendorUpdateRequest;
import com.vertexone.prod.vendor.dto.request.VendorStatusUpdateRequest;
import com.vertexone.prod.vendor.dto.response.VendorResponse;
import com.vertexone.prod.vendor.service.VendorService;
import com.vertexone.prod.vendor.enums.VendorStatus;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse createVendor(
            @PathVariable UUID organizationId,
            @Valid @RequestBody VendorCreateRequest request,
            Authentication authentication) {

        return vendorService.createVendor(
                organizationId,
                request,
                authentication.getName()
        );
    }


    @GetMapping("/search")
    public Page<VendorResponse> searchVendors(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VendorStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            Authentication authentication) {

        return vendorService.searchVendors(
                organizationId,
                keyword,
                status,
                pageable,
                authentication.getName()
        );
    }


    @GetMapping
    public List<VendorResponse> getOrganizationVendors(
            @PathVariable UUID organizationId,
            Authentication authentication) {

        return vendorService.getOrganizationVendors(
                organizationId,
                authentication.getName()
        );
    }

    @GetMapping("/{vendorId}")
    public VendorResponse getVendor(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            Authentication authentication) {

        return vendorService.getVendor(
                organizationId,
                vendorId,
                authentication.getName()
        );
    }

    @PutMapping("/{vendorId}")
    public VendorResponse updateVendor(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorUpdateRequest request,
            Authentication authentication) {

        return vendorService.updateVendor(
                organizationId,
                vendorId,
                request,
                authentication.getName()
        );
    }

    @PatchMapping("/{vendorId}/status")
    public VendorResponse updateVendorStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorStatusUpdateRequest request,
            Authentication authentication) {

        return vendorService.updateVendorStatus(
                organizationId,
                vendorId,
                request.status(),
                authentication.getName()
        );
    }
}

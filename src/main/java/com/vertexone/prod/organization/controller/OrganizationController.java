
package com.vertexone.prod.organization.controller;

import com.vertexone.prod.organization.dto.request.OrganizationCreateRequest;
import com.vertexone.prod.organization.dto.response.OrganizationResponse;
import com.vertexone.prod.organization.service.OrganizationService;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(
                        request,
                        authentication.getName()
                ));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySlug(
            @PathVariable String slug,
            Authentication authentication) {

        return ResponseEntity.ok(
                organizationService.getOrganizationBySlug(
                        slug,
                        authentication.getName()
                )
        );
    }
}

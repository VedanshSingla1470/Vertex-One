
package com.vertexone.prod.organization.controller;

import com.vertexone.prod.organization.dto.request.OrganizationCreateRequest;
import com.vertexone.prod.organization.dto.response.OrganizationResponse;
import com.vertexone.prod.organization.service.OrganizationService;

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
            @Valid @RequestBody OrganizationCreateRequest request) {

        OrganizationResponse response =
                organizationService.createOrganization(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganization(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                organizationService.getOrganizationBySlug(slug)
        );
    }
}

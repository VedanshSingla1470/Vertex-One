
package com.vertexone.prod.membership.controller;

import com.vertexone.prod.membership.dto.request.MembershipCreateRequest;
import com.vertexone.prod.membership.dto.request.MembershipRoleUpdateRequest;
import com.vertexone.prod.membership.dto.response.MembershipResponse;
import com.vertexone.prod.membership.dto.request.MembershipStatusUpdateRequest;
import com.vertexone.prod.membership.service.MembershipService;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<MembershipResponse> createMembership(
            @Valid @RequestBody MembershipCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(membershipService.createMembership(
                        request,
                        authentication.getName()
                ));
    }


    @GetMapping("/organizations/{organizationId}/memberships")
    public ResponseEntity<List<MembershipResponse>> getOrganizationMembers(
            @PathVariable UUID organizationId,
            Authentication authentication) {

        return ResponseEntity.ok(
                membershipService.getOrganizationMembers(
                        organizationId,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/organizations/{organizationId}/memberships/{membershipId}/role")
    public ResponseEntity<MembershipResponse> updateMemberRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId,
            @Valid @RequestBody MembershipRoleUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                membershipService.updateMemberRole(
                        organizationId,
                        membershipId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/organizations/{organizationId}/memberships/{membershipId}/status")
    public ResponseEntity<MembershipResponse> updateMemberStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId,
            @Valid @RequestBody MembershipStatusUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                membershipService.updateMemberStatus(
                        organizationId,
                        membershipId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/organizations/{organizationId}/memberships/{membershipId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID membershipId,
            Authentication authentication) {

        membershipService.removeMember(
                organizationId,
                membershipId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

}

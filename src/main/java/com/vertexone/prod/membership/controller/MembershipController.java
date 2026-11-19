
package com.vertexone.prod.membership.controller;

import com.vertexone.prod.membership.dto.request.MembershipCreateRequest;
import com.vertexone.prod.membership.dto.response.MembershipResponse;
import com.vertexone.prod.membership.service.MembershipService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<MembershipResponse> createMembership(
            @Valid @RequestBody MembershipCreateRequest request) {

        MembershipResponse response =
                membershipService.createMembership(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

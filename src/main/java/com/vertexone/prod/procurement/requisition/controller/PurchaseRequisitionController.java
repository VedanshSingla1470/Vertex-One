package com.vertexone.prod.procurement.requisition.controller;

import com.vertexone.prod.procurement.requisition.dto.request.CreateRequisitionRequest;
import com.vertexone.prod.procurement.requisition.dto.request.RejectRequisitionRequest;
import com.vertexone.prod.procurement.requisition.dto.response.RequisitionResponse;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;
import com.vertexone.prod.procurement.requisition.service.PurchaseRequisitionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/purchase-requisitions")
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService requisitionService;

    public PurchaseRequisitionController(
            PurchaseRequisitionService requisitionService
    ) {
        this.requisitionService = requisitionService;
    }

    @PostMapping
    public RequisitionResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateRequisitionRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.create(
                organizationId,
                principal.getUsername(),
                request
        );
    }

    @GetMapping
    public Page<RequisitionResponse> findAll(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) RequisitionStatus status,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.findAll(
                organizationId,
                principal.getUsername(),
                status,
                pageable
        );
    }

    @GetMapping("/{requisitionId}")
    public RequisitionResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID requisitionId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.findById(
                organizationId,
                requisitionId,
                principal.getUsername()
        );
    }

    @PostMapping("/{requisitionId}/submit")
    public RequisitionResponse submit(
            @PathVariable UUID organizationId,
            @PathVariable UUID requisitionId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.submit(
                organizationId,
                requisitionId,
                principal.getUsername()
        );
    }

    @PostMapping("/{requisitionId}/approve")
    public RequisitionResponse approve(
            @PathVariable UUID organizationId,
            @PathVariable UUID requisitionId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.approve(
                organizationId,
                requisitionId,
                principal.getUsername()
        );
    }

    @PostMapping("/{requisitionId}/reject")
    public RequisitionResponse reject(
            @PathVariable UUID organizationId,
            @PathVariable UUID requisitionId,
            @Valid @RequestBody RejectRequisitionRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.reject(
                organizationId,
                requisitionId,
                principal.getUsername(),
                request.reason()
        );
    }

    @PostMapping("/{requisitionId}/cancel")
    public RequisitionResponse cancel(
            @PathVariable UUID organizationId,
            @PathVariable UUID requisitionId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return requisitionService.cancel(
                organizationId,
                requisitionId,
                principal.getUsername()
        );
    }
}
package com.vertexone.prod.procurement.purchaseorder.controller;

import com.vertexone.prod.procurement.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.vertexone.prod.procurement.purchaseorder.dto.response.PurchaseOrderResponse;
import com.vertexone.prod.procurement.purchaseorder.enums.PurchaseOrderStatus;
import com.vertexone.prod.procurement.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService
    ) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public PurchaseOrderResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.create(
                organizationId,
                principal.getUsername(),
                request
        );
    }

    @GetMapping
    public Page<PurchaseOrderResponse> findAll(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.findAll(
                organizationId,
                principal.getUsername(),
                status,
                pageable
        );
    }

    @GetMapping("/{purchaseOrderId}")
    public PurchaseOrderResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.findById(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }

    @PostMapping("/{purchaseOrderId}/submit-for-approval")
    public PurchaseOrderResponse submitForApproval(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.submitForApproval(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }

    @PostMapping("/{purchaseOrderId}/approve")
    public PurchaseOrderResponse approve(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.approve(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }

    @PostMapping("/{purchaseOrderId}/send")
    public PurchaseOrderResponse sendToVendor(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.sendToVendor(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }

    @PostMapping("/{purchaseOrderId}/acknowledge")
    public PurchaseOrderResponse acknowledge(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.acknowledge(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }

    @PostMapping("/{purchaseOrderId}/cancel")
    public PurchaseOrderResponse cancel(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return purchaseOrderService.cancel(
                organizationId,
                purchaseOrderId,
                principal.getUsername()
        );
    }
}
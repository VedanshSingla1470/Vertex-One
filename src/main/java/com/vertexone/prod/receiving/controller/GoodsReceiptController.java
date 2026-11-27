package com.vertexone.prod.receiving.controller;

import com.vertexone.prod.receiving.dto.request.CreateGoodsReceiptRequest;
import com.vertexone.prod.receiving.dto.response.GoodsReceiptResponse;
import com.vertexone.prod.receiving.service.GoodsReceiptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService) {
        this.goodsReceiptService = goodsReceiptService;
    }

    @PostMapping("/purchase-orders/{purchaseOrderId}/receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceiptResponse createReceipt(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            @Valid @RequestBody CreateGoodsReceiptRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return goodsReceiptService.createReceipt(
                organizationId,
                purchaseOrderId,
                userDetails.getUsername(),
                request
        );
    }

    @GetMapping("/purchase-orders/{purchaseOrderId}/receipts")
    public Page<GoodsReceiptResponse> getPurchaseOrderReceipts(
            @PathVariable UUID organizationId,
            @PathVariable UUID purchaseOrderId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return goodsReceiptService.getReceiptsForPurchaseOrder(
                organizationId,
                purchaseOrderId,
                userDetails.getUsername(),
                pageable
        );
    }

    @GetMapping("/receipts")
    public Page<GoodsReceiptResponse> getReceipts(
            @PathVariable UUID organizationId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return goodsReceiptService.getReceipts(
                organizationId,
                userDetails.getUsername(),
                pageable
        );
    }

    @GetMapping("/receipts/{receiptId}")
    public GoodsReceiptResponse getReceipt(
            @PathVariable UUID organizationId,
            @PathVariable UUID receiptId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return goodsReceiptService.getReceipt(
                organizationId,
                receiptId,
                userDetails.getUsername()
        );
    }
}
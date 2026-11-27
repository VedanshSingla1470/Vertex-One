package com.vertexone.prod.receiving.dto.response;

import com.vertexone.prod.receiving.entity.GoodsReceipt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GoodsReceiptResponse(
        UUID id,
        UUID organizationId,
        UUID purchaseOrderId,
        String purchaseOrderNumber,
        UUID warehouseId,
        String receiptNumber,
        String deliveryNoteNumber,
        UUID receivedByUserId,
        Instant receivedAt,
        String notes,
        List<GoodsReceiptItemResponse> items
) {
    public static GoodsReceiptResponse from(GoodsReceipt receipt) {
        return new GoodsReceiptResponse(
                receipt.getId(),
                receipt.getOrganization().getId(),
                receipt.getPurchaseOrder().getId(),
                receipt.getPurchaseOrder().getPurchaseOrderNumber(),
                receipt.getWarehouseId(),
                receipt.getReceiptNumber(),
                receipt.getDeliveryNoteNumber(),
                receipt.getReceivedBy().getId(),
                receipt.getReceivedAt(),
                receipt.getNotes(),
                receipt.getItems()
                        .stream()
                        .map(GoodsReceiptItemResponse::from)
                        .toList()
        );
    }
}
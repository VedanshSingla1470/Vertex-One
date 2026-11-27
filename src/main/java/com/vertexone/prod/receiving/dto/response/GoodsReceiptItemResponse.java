package com.vertexone.prod.receiving.dto.response;

import com.vertexone.prod.receiving.entity.GoodsReceiptItem;

import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemResponse(
        UUID id,
        UUID purchaseOrderItemId,
        UUID productId,
        String productName,
        String sku,
        BigDecimal quantityReceived,
        String notes
) {
    public static GoodsReceiptItemResponse from(GoodsReceiptItem item) {
        return new GoodsReceiptItemResponse(
                item.getId(),
                item.getPurchaseOrderItem().getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantityReceived(),
                item.getNotes()
        );
    }
}
package com.vertexone.prod.procurement.purchaseorder.dto.response;

import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrder;
import com.vertexone.prod.procurement.purchaseorder.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(

        UUID id,
        String purchaseOrderNumber,
        UUID requisitionId,
        UUID vendorId,
        String vendorName,
        UUID warehouseId,
        PurchaseOrderStatus status,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        String currencyCode,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        String notes,
        UUID createdByUserId,
        UUID approvedByUserId,
        Instant approvedAt,
        Instant sentAt,
        List<ItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {

    public record ItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String sku,
            BigDecimal orderedQuantity,
            BigDecimal receivedQuantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal lineSubtotal,
            BigDecimal taxAmount,
            BigDecimal lineTotal,
            String notes
    ) {}

    public static PurchaseOrderResponse from(PurchaseOrder order) {

        List<ItemResponse> items = order.getItems()
                .stream()
                .map(item -> new ItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getOrderedQuantity(),
                        item.getReceivedQuantity(),
                        item.getUnitPrice(),
                        item.getTaxRate(),
                        item.getLineSubtotal(),
                        item.getTaxAmount(),
                        item.getLineTotal(),
                        item.getNotes()
                ))
                .toList();

        return new PurchaseOrderResponse(
                order.getId(),
                order.getPurchaseOrderNumber(),
                order.getRequisition() != null
                        ? order.getRequisition().getId()
                        : null,
                order.getVendor().getId(),
                order.getVendor().getName(),
                order.getWarehouseId(),
                order.getStatus(),
                order.getOrderDate(),
                order.getExpectedDeliveryDate(),
                order.getCurrencyCode(),
                order.getSubtotal(),
                order.getTaxTotal(),
                order.getGrandTotal(),
                order.getNotes(),
                order.getCreatedBy().getId(),
                order.getApprovedBy() != null
                        ? order.getApprovedBy().getId()
                        : null,
                order.getApprovedAt(),
                order.getSentAt(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
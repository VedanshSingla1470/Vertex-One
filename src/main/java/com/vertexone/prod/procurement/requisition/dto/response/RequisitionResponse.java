package com.vertexone.prod.procurement.requisition.dto.response;

import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisition;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RequisitionResponse(

        UUID id,
        String requisitionNumber,
        String title,
        String description,
        RequisitionStatus status,
        UUID requestedByUserId,
        UUID reviewedByUserId,
        Instant reviewedAt,
        String rejectionReason,
        List<ItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {

    public record ItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String sku,
            BigDecimal requestedQuantity,
            BigDecimal approvedQuantity,
            String notes
    ) {}

    public static RequisitionResponse from(PurchaseRequisition requisition) {

        List<ItemResponse> items = requisition.getItems()
                .stream()
                .map(item -> new ItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getRequestedQuantity(),
                        item.getApprovedQuantity(),
                        item.getNotes()
                ))
                .toList();

        return new RequisitionResponse(
                requisition.getId(),
                requisition.getRequisitionNumber(),
                requisition.getTitle(),
                requisition.getDescription(),
                requisition.getStatus(),
                requisition.getRequestedBy().getId(),
                requisition.getReviewedBy() != null
                        ? requisition.getReviewedBy().getId()
                        : null,
                requisition.getReviewedAt(),
                requisition.getRejectionReason(),
                items,
                requisition.getCreatedAt(),
                requisition.getUpdatedAt()
        );
    }
}
package com.vertexone.prod.receiving.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemRequest(

        @NotNull
        UUID purchaseOrderItemId,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal quantityReceived,

        @Size(max = 500)
        String notes

) {
}
package com.vertexone.prod.procurement.purchaseorder.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0.0000")
        BigDecimal unitPrice,

        @NotNull
        @DecimalMin(value = "0.0000")
        BigDecimal taxRate,

        @Size(max = 500)
        String notes

) {}
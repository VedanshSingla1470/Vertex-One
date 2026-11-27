package com.vertexone.prod.procurement.requisition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RequisitionItemRequest(

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal requestedQuantity,

        @Size(max = 500)
        String notes

) {}
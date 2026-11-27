package com.vertexone.prod.procurement.purchaseorder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseOrderRequest(

        UUID requisitionId,

        @NotNull
        UUID vendorId,

        UUID warehouseId,

        @NotNull
        LocalDate orderDate,

        LocalDate expectedDeliveryDate,

        @NotBlank
        @Size(min = 3, max = 3)
        String currencyCode,

        String notes,

        @NotEmpty
        List<@Valid PurchaseOrderItemRequest> items

) {}
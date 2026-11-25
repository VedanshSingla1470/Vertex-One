package com.vertexone.prod.product.dto.request;

import com.vertexone.prod.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(

        @NotNull
        ProductStatus status

) {}

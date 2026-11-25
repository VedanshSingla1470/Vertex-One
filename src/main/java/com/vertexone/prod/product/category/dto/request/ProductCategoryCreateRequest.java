package com.vertexone.prod.product.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductCategoryCreateRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description

) {}

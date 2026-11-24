package com.vertexone.prod.vendor.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorCategoryCreateRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description

) {}
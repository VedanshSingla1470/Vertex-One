package com.vertexone.prod.product.dto.request;

import com.vertexone.prod.product.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductCreateRequest(

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$")
        String sku,

        @NotBlank
        @Size(max = 150)
        String name,

        String description,

        UUID categoryId,

        @NotNull
        UnitOfMeasure unitOfMeasure

) {}

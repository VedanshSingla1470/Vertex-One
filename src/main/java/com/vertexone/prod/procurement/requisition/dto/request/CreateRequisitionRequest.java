package com.vertexone.prod.procurement.requisition.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRequisitionRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        String description,

        @NotEmpty
        List<@Valid RequisitionItemRequest> items

) {}
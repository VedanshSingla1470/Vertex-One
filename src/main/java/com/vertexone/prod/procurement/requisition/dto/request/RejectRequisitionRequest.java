package com.vertexone.prod.procurement.requisition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequisitionRequest(

        @NotBlank
        @Size(max = 500)
        String reason

) {}
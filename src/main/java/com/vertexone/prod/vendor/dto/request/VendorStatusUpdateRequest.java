
package com.vertexone.prod.vendor.dto.request;

import com.vertexone.prod.vendor.enums.VendorStatus;
import jakarta.validation.constraints.NotNull;

public record VendorStatusUpdateRequest(

        @NotNull(message = "Vendor status is required")
        VendorStatus status

) {}


package com.vertexone.prod.vendor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorUpdateRequest(

        @NotBlank(message = "Vendor name is required")
        @Size(max = 150)
        String name,

        @Email(message = "Enter a valid email address")
        @Size(max = 254)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 255)
        String website,

        String address,

        @Size(max = 50)
        String taxId

) {}

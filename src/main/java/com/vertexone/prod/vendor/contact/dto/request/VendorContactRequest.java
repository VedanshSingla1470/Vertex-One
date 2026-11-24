package com.vertexone.prod.vendor.contact.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorContactRequest(

        @NotBlank(message = "Contact name is required")
        @Size(max = 120)
        String name,

        @Email(message = "Enter a valid email address")
        @Size(max = 254)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String designation,

        boolean primary

) {}

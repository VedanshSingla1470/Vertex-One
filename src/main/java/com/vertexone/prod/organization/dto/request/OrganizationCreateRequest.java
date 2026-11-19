
package com.vertexone.prod.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizationCreateRequest(

        @NotBlank(message = "Organization name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Organization slug is required")
        @Size(max = 100)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must contain lowercase letters, numbers and hyphens"
        )
        String slug,

        @Size(max = 255)
        String description

) {}

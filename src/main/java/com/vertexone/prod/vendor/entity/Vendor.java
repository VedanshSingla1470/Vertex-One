
package com.vertexone.prod.vendor.entity;

import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.vendor.enums.VendorStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vendors",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vendors_organization_tax_id",
                        columnNames = {"organization_id", "tax_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_vendors_organization_id",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_vendors_name",
                        columnList = "name"
                ),
                @Index(
                        name = "idx_vendors_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VendorStatus status = VendorStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

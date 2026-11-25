package com.vertexone.prod.product.entity;

import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.product.category.entity.ProductCategory;
import com.vertexone.prod.product.enums.ProductStatus;
import com.vertexone.prod.product.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_org_sku",
                        columnNames = {"organization_id", "sku"}
                )
        },
        indexes = {
                @Index(name = "idx_products_organization_id",
                        columnList = "organization_id"),
                @Index(name = "idx_products_category_id",
                        columnList = "category_id"),
                @Index(name = "idx_products_name",
                        columnList = "name"),
                @Index(name = "idx_products_status",
                        columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 30)
    private UnitOfMeasure unitOfMeasure;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;

        if (sku != null) {
            sku = sku.trim().toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();

        if (sku != null) {
            sku = sku.trim().toUpperCase();
        }
    }
}

package com.vertexone.prod.inventory.warehouse.entity;

import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import com.vertexone.prod.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "warehouses",
        schema = "app",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_warehouses_org_code",
                columnNames = {"organization_id", "code"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarehouseStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = WarehouseStatus.ACTIVE;
        if (code != null) code = code.trim().toUpperCase();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        if (code != null) code = code.trim().toUpperCase();
    }
}
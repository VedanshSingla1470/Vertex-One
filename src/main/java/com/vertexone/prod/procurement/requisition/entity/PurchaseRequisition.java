package com.vertexone.prod.procurement.requisition.entity;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "purchase_requisitions",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_requisitions_org_number",
                        columnNames = {"organization_id", "requisition_number"}
                )
        }
)
public class PurchaseRequisition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "requisition_number", nullable = false, length = 50)
    private String requisitionNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequisitionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Builder.Default
    @OneToMany(
            mappedBy = "requisition",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseRequisitionItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = RequisitionStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(PurchaseRequisitionItem item) {
        items.add(item);
        item.setRequisition(this);
        item.setOrganization(organization);
    }
}

package com.vertexone.prod.membership.entity;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.enums.MembershipStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "memberships",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_membership_user_organization",
                        columnNames = {"user_id", "organization_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_memberships_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_memberships_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MembershipRole role;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "removed_by_user_id")
    private User removedBy;

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

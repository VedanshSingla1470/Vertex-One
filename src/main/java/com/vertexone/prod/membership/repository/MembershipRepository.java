
package com.vertexone.prod.membership.repository;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.membership.enums.MembershipStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository
        extends JpaRepository<Membership, UUID> {

    List<Membership> findByUserId(UUID userId);

    List<Membership> findByOrganizationId(UUID organizationId);

    Optional<Membership> findByUserIdAndOrganizationId(
            UUID userId,
            UUID organizationId
    );

    boolean existsByUserIdAndOrganizationId(
            UUID userId,
            UUID organizationId
    );

    List<Membership> findByUserIdAndStatus(
            UUID userId,
            MembershipStatus status
    );
}

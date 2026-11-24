
package com.vertexone.prod.vendor.repository;

import com.vertexone.prod.vendor.entity.Vendor;
import com.vertexone.prod.vendor.enums.VendorStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {


    @Query("""
        SELECT v FROM Vendor v
        WHERE v.organization.id = :organizationId
        AND (
            :keyword IS NULL
            OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(v.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(v.taxId) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :status IS NULL
            OR v.status = :status
        )
        """)
    Page<Vendor> searchVendors(
            @Param("organizationId") UUID organizationId,
            @Param("keyword") String keyword,
            @Param("status") VendorStatus status,
            Pageable pageable
    );


    List<Vendor> findByOrganizationId(UUID organizationId);

    List<Vendor> findByOrganizationIdAndStatus(
            UUID organizationId,
            VendorStatus status
    );

    Optional<Vendor> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndTaxId(
            UUID organizationId,
            String taxId
    );

    boolean existsByOrganizationIdAndTaxIdAndIdNot(
            UUID organizationId,
            String taxId,
            UUID id
    );
}

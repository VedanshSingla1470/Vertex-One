package com.vertexone.prod.vendor.category.repository;

import com.vertexone.prod.vendor.category.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorCategoryRepository
        extends JpaRepository<VendorCategory, UUID> {

    List<VendorCategory> findByOrganizationId(UUID organizationId);

    Optional<VendorCategory> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
            UUID organizationId,
            String name,
            UUID id
    );

}
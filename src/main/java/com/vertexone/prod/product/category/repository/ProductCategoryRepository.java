package com.vertexone.prod.product.category.repository;

import com.vertexone.prod.product.category.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findAllByOrganizationIdOrderByNameAsc(
            UUID organizationId
    );

    Optional<ProductCategory> findByIdAndOrganizationId(
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

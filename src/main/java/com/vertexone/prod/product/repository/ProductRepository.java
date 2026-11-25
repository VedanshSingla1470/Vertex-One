
package com.vertexone.prod.product.repository;

import com.vertexone.prod.product.entity.Product;
import com.vertexone.prod.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndSku(
            UUID organizationId,
            String sku
    );

    boolean existsByOrganizationIdAndSkuAndIdNot(
            UUID organizationId,
            String sku,
            UUID id
    );

    boolean existsByOrganizationIdAndCategoryId(
            UUID organizationId,
            UUID categoryId
    );

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.organization.id = :organizationId

            AND (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(p.description, ''))
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )

            AND (
                :status IS NULL
                OR p.status = :status
            )

            AND (
                :categoryId IS NULL
                OR p.category.id = :categoryId
            )
            """)
    Page<Product> searchProducts(
            @Param("organizationId") UUID organizationId,
            @Param("keyword") String keyword,
            @Param("status") ProductStatus status,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );
}

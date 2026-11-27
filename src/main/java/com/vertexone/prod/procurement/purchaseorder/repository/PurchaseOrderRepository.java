package com.vertexone.prod.procurement.purchaseorder.repository;

import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrder;
import com.vertexone.prod.procurement.purchaseorder.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT po
    FROM PurchaseOrder po
    WHERE po.id = :purchaseOrderId
      AND po.organization.id = :organizationId
    """)
    Optional<PurchaseOrder> findByIdAndOrganizationIdForUpdate(
            @Param("purchaseOrderId") UUID purchaseOrderId,
            @Param("organizationId") UUID organizationId
    );

    Page<PurchaseOrder> findByOrganizationId(
            UUID organizationId,
            Pageable pageable
    );

    Page<PurchaseOrder> findByOrganizationIdAndStatus(
            UUID organizationId,
            PurchaseOrderStatus status,
            Pageable pageable
    );

    Optional<PurchaseOrder> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndPurchaseOrderNumber(
            UUID organizationId,
            String purchaseOrderNumber
    );
}
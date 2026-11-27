package com.vertexone.prod.procurement.requisition.repository;

import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisition;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRequisitionRepository
        extends JpaRepository<PurchaseRequisition, UUID> {

    Page<PurchaseRequisition> findByOrganizationId(
            UUID organizationId,
            Pageable pageable
    );

    Page<PurchaseRequisition> findByOrganizationIdAndStatus(
            UUID organizationId,
            RequisitionStatus status,
            Pageable pageable
    );

    Optional<PurchaseRequisition> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndRequisitionNumber(
            UUID organizationId,
            String requisitionNumber
    );
}
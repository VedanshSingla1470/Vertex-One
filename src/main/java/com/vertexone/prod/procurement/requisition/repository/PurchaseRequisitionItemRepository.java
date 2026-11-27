package com.vertexone.prod.procurement.requisition.repository;

import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisitionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRequisitionItemRepository
        extends JpaRepository<PurchaseRequisitionItem, UUID> {

    List<PurchaseRequisitionItem> findByRequisitionId(UUID requisitionId);
}
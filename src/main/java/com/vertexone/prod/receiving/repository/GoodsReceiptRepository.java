package com.vertexone.prod.receiving.repository;

import com.vertexone.prod.receiving.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GoodsReceiptRepository
        extends JpaRepository<GoodsReceipt, UUID> {

    Page<GoodsReceipt> findByOrganizationIdOrderByReceivedAtDesc(
            UUID organizationId,
            Pageable pageable
    );

    Page<GoodsReceipt> findByOrganizationIdAndPurchaseOrderIdOrderByReceivedAtDesc(
            UUID organizationId,
            UUID purchaseOrderId,
            Pageable pageable
    );

    java.util.Optional<GoodsReceipt> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndReceiptNumber(
            UUID organizationId,
            String receiptNumber
    );
}
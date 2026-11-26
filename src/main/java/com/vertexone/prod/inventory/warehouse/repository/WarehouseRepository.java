package com.vertexone.prod.inventory.warehouse.repository;

import com.vertexone.prod.inventory.warehouse.entity.Warehouse;
import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Page<Warehouse> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Warehouse> findByOrganizationIdAndStatus(
            UUID organizationId, WarehouseStatus status, Pageable pageable
    );

    Optional<Warehouse> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndCode(UUID organizationId, String code);

    boolean existsByOrganizationIdAndCodeAndIdNot(
            UUID organizationId, String code, UUID id
    );
}
package com.vertexone.prod.inventory.warehouse.service;

import com.vertexone.prod.inventory.service.InventoryAuthorizationService;
import com.vertexone.prod.inventory.warehouse.dto.request.*;
import com.vertexone.prod.inventory.warehouse.dto.response.WarehouseResponse;
import com.vertexone.prod.inventory.warehouse.entity.Warehouse;
import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import com.vertexone.prod.inventory.warehouse.repository.WarehouseRepository;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final OrganizationRepository organizationRepository;
    private final InventoryAuthorizationService authorizationService;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            OrganizationRepository organizationRepository,
            InventoryAuthorizationService authorizationService) {
        this.warehouseRepository = warehouseRepository;
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
    }

    public WarehouseResponse create(
            UUID organizationId, String actorEmail,
            CreateWarehouseRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        String code = normalizeCode(request.code());

        if (warehouseRepository.existsByOrganizationIdAndCode(organizationId, code)) {
            throw new IllegalStateException("A warehouse with this code already exists.");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));

        Warehouse warehouse = Warehouse.builder()
                .organization(organization)
                .name(request.name().trim())
                .code(code)
                .description(request.description())
                .address(request.address())
                .status(WarehouseStatus.ACTIVE)
                .build();

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public Page<WarehouseResponse> list(
            UUID organizationId, String actorEmail,
            WarehouseStatus status, Pageable pageable) {

        authorizationService.requireViewAccess(actorEmail, organizationId);

        Page<Warehouse> warehouses = status == null
                ? warehouseRepository.findByOrganizationId(organizationId, pageable)
                : warehouseRepository.findByOrganizationIdAndStatus(
                organizationId, status, pageable
        );

        return warehouses.map(this::toResponse);
    }

    public WarehouseResponse update(
            UUID organizationId, UUID warehouseId, String actorEmail,
            UpdateWarehouseRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getWarehouse(organizationId, warehouseId);
        String code = normalizeCode(request.code());

        if (warehouseRepository.existsByOrganizationIdAndCodeAndIdNot(
                organizationId, code, warehouseId)) {
            throw new IllegalStateException("Another warehouse already uses this code.");
        }

        warehouse.setName(request.name().trim());
        warehouse.setCode(code);
        warehouse.setDescription(request.description());
        warehouse.setAddress(request.address());

        return toResponse(warehouseRepository.save(warehouse));
    }

    public WarehouseResponse deactivate(
            UUID organizationId, UUID warehouseId, String actorEmail) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getWarehouse(organizationId, warehouseId);
        warehouse.setStatus(WarehouseStatus.INACTIVE);

        return toResponse(warehouseRepository.save(warehouse));
    }

    public Warehouse getWarehouse(UUID organizationId, UUID warehouseId) {
        return warehouseRepository.findByIdAndOrganizationId(
                warehouseId, organizationId
        ).orElseThrow(() -> new IllegalArgumentException("Warehouse not found."));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getOrganization().getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getDescription(),
                warehouse.getAddress(),
                warehouse.getStatus(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}
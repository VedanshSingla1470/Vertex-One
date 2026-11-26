package com.vertexone.prod.inventory.warehouse.controller;

import com.vertexone.prod.inventory.warehouse.dto.request.*;
import com.vertexone.prod.inventory.warehouse.dto.response.WarehouseResponse;
import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import com.vertexone.prod.inventory.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody CreateWarehouseRequest request) {

        return warehouseService.create(
                organizationId, authentication.getName(), request
        );
    }

    @GetMapping
    public Page<WarehouseResponse> list(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @RequestParam(required = false) WarehouseStatus status,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return warehouseService.list(
                organizationId, authentication.getName(), status, pageable
        );
    }

    @PutMapping("/{warehouseId}")
    public WarehouseResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID warehouseId,
            Authentication authentication,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        return warehouseService.update(
                organizationId, warehouseId, authentication.getName(), request
        );
    }

    @PatchMapping("/{warehouseId}/deactivate")
    public WarehouseResponse deactivate(
            @PathVariable UUID organizationId,
            @PathVariable UUID warehouseId,
            Authentication authentication) {

        return warehouseService.deactivate(
                organizationId, warehouseId, authentication.getName()
        );
    }
}
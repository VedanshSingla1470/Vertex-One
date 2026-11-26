package com.vertexone.prod.inventory.balance.controller;

import com.vertexone.prod.inventory.balance.dto.request.UpdateReorderLevelRequest;
import com.vertexone.prod.inventory.balance.dto.response.InventoryBalanceResponse;
import com.vertexone.prod.inventory.balance.service.InventoryService;
import com.vertexone.prod.inventory.movement.dto.request.StockOperationRequest;
import com.vertexone.prod.inventory.movement.dto.request.StockTransferRequest;
import com.vertexone.prod.inventory.movement.dto.response.StockMovementResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/inventory/receive")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse receive(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody StockOperationRequest request) {

        return inventoryService.receive(
                organizationId, authentication.getName(), request
        );
    }

    @PostMapping("/inventory/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse issue(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody StockOperationRequest request) {

        return inventoryService.issue(
                organizationId, authentication.getName(), request
        );
    }

    @PostMapping("/inventory/adjustments/increase")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse adjustIn(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody StockOperationRequest request) {

        return inventoryService.adjustIn(
                organizationId, authentication.getName(), request
        );
    }

    @PostMapping("/inventory/adjustments/decrease")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse adjustOut(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody StockOperationRequest request) {

        return inventoryService.adjustOut(
                organizationId, authentication.getName(), request
        );
    }

    @PostMapping("/inventory/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse transfer(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @Valid @RequestBody StockTransferRequest request) {

        return inventoryService.transfer(
                organizationId, authentication.getName(), request
        );
    }

    @GetMapping("/warehouses/{warehouseId}/inventory")
    public Page<InventoryBalanceResponse> listBalances(
            @PathVariable UUID organizationId,
            @PathVariable UUID warehouseId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "updatedAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return inventoryService.listBalances(
                organizationId, authentication.getName(), warehouseId, pageable
        );
    }

    @GetMapping("/inventory/low-stock")
    public Page<InventoryBalanceResponse> lowStock(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @RequestParam(required = false) UUID warehouseId,
            @PageableDefault(size = 20, sort = "updatedAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return inventoryService.lowStock(
                organizationId, authentication.getName(), warehouseId, pageable
        );
    }

    @PutMapping("/warehouses/{warehouseId}/inventory/reorder-level")
    public InventoryBalanceResponse updateReorderLevel(
            @PathVariable UUID organizationId,
            @PathVariable UUID warehouseId,
            Authentication authentication,
            @Valid @RequestBody UpdateReorderLevelRequest request) {

        return inventoryService.updateReorderLevel(
                organizationId, authentication.getName(), warehouseId, request
        );
    }

    @GetMapping("/stock-movements")
    public Page<StockMovementResponse> movementHistory(
            @PathVariable UUID organizationId,
            Authentication authentication,
            @RequestParam(required = false) UUID productId,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return inventoryService.movementHistory(
                organizationId, authentication.getName(), productId, pageable
        );
    }
}
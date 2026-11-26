package com.vertexone.prod.inventory.balance.service;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.inventory.balance.dto.request.UpdateReorderLevelRequest;
import com.vertexone.prod.inventory.balance.dto.response.InventoryBalanceResponse;
import com.vertexone.prod.inventory.balance.entity.InventoryBalance;
import com.vertexone.prod.inventory.balance.repository.InventoryBalanceRepository;
import com.vertexone.prod.inventory.movement.dto.request.*;
import com.vertexone.prod.inventory.movement.dto.response.StockMovementResponse;
import com.vertexone.prod.inventory.movement.entity.StockMovement;
import com.vertexone.prod.inventory.movement.enums.StockMovementType;
import com.vertexone.prod.inventory.movement.repository.StockMovementRepository;
import com.vertexone.prod.inventory.service.InventoryAuthorizationService;
import com.vertexone.prod.inventory.warehouse.entity.Warehouse;
import com.vertexone.prod.inventory.warehouse.enums.WarehouseStatus;
import com.vertexone.prod.inventory.warehouse.repository.WarehouseRepository;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.product.entity.Product;
import com.vertexone.prod.product.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryAuthorizationService authorizationService;

    public InventoryService(
            InventoryBalanceRepository balanceRepository,
            StockMovementRepository movementRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            InventoryAuthorizationService authorizationService) {
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    public StockMovementResponse receive(
            UUID organizationId, String actorEmail,
            StockOperationRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getActiveWarehouse(
                organizationId, request.warehouseId()
        );
        Product product = getProduct(organizationId, request.productId());

        InventoryBalance balance = getOrCreateBalance(
                organizationId, warehouse, product
        );

        balance.setQuantityOnHand(
                balance.getQuantityOnHand().add(request.quantity())
        );

        balanceRepository.save(balance);

        return recordMovement(
                organizationId, product, null, warehouse,
                StockMovementType.RECEIPT, request, actorEmail
        );
    }

    public StockMovementResponse issue(
            UUID organizationId, String actorEmail,
            StockOperationRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getActiveWarehouse(
                organizationId, request.warehouseId()
        );
        Product product = getProduct(organizationId, request.productId());

        InventoryBalance balance = getOrCreateBalance(
                organizationId, warehouse, product
        );

        subtractStock(balance, request.quantity());
        balanceRepository.save(balance);

        return recordMovement(
                organizationId, product, warehouse, null,
                StockMovementType.ISSUE, request, actorEmail
        );
    }

    public StockMovementResponse adjustIn(
            UUID organizationId, String actorEmail,
            StockOperationRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getActiveWarehouse(
                organizationId, request.warehouseId()
        );
        Product product = getProduct(organizationId, request.productId());

        InventoryBalance balance = getOrCreateBalance(
                organizationId, warehouse, product
        );

        balance.setQuantityOnHand(
                balance.getQuantityOnHand().add(request.quantity())
        );
        balanceRepository.save(balance);

        return recordMovement(
                organizationId, product, null, warehouse,
                StockMovementType.ADJUSTMENT_IN, request, actorEmail
        );
    }

    public StockMovementResponse adjustOut(
            UUID organizationId, String actorEmail,
            StockOperationRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        Warehouse warehouse = getActiveWarehouse(
                organizationId, request.warehouseId()
        );
        Product product = getProduct(organizationId, request.productId());

        InventoryBalance balance = getOrCreateBalance(
                organizationId, warehouse, product
        );

        subtractStock(balance, request.quantity());
        balanceRepository.save(balance);

        return recordMovement(
                organizationId, product, warehouse, null,
                StockMovementType.ADJUSTMENT_OUT, request, actorEmail
        );
    }

    public StockMovementResponse transfer(
            UUID organizationId, String actorEmail,
            StockTransferRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        if (request.sourceWarehouseId().equals(request.destinationWarehouseId())) {
            throw new IllegalArgumentException(
                    "Source and destination warehouses must be different."
            );
        }

        // Lock balances in a consistent order to reduce deadlock risk.
        UUID firstId = request.sourceWarehouseId().compareTo(
                request.destinationWarehouseId()
        ) < 0 ? request.sourceWarehouseId() : request.destinationWarehouseId();

        UUID secondId = firstId.equals(request.sourceWarehouseId())
                ? request.destinationWarehouseId() : request.sourceWarehouseId();

        Warehouse firstWarehouse = getActiveWarehouse(organizationId, firstId);
        Warehouse secondWarehouse = getActiveWarehouse(organizationId, secondId);

        Product product = getProduct(organizationId, request.productId());

        InventoryBalance firstBalance = getOrCreateBalance(
                organizationId, firstWarehouse, product
        );
        InventoryBalance secondBalance = getOrCreateBalance(
                organizationId, secondWarehouse, product
        );

        InventoryBalance sourceBalance = firstWarehouse.getId().equals(
                request.sourceWarehouseId()
        ) ? firstBalance : secondBalance;

        InventoryBalance destinationBalance = firstWarehouse.getId().equals(
                request.destinationWarehouseId()
        ) ? firstBalance : secondBalance;

        subtractStock(sourceBalance, request.quantity());

        destinationBalance.setQuantityOnHand(
                destinationBalance.getQuantityOnHand().add(request.quantity())
        );

        balanceRepository.save(sourceBalance);
        balanceRepository.save(destinationBalance);

        User user = getUser(actorEmail);

        StockMovement movement = StockMovement.builder()
                .organization(sourceBalance.getOrganization())
                .product(product)
                .sourceWarehouse(getActiveWarehouse(
                        organizationId, request.sourceWarehouseId()
                ))
                .destinationWarehouse(getActiveWarehouse(
                        organizationId, request.destinationWarehouseId()
                ))
                .movementType(StockMovementType.TRANSFER)
                .quantity(request.quantity())
                .referenceNumber(request.referenceNumber())
                .reason(request.reason())
                .performedBy(user)
                .build();

        return toMovementResponse(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public Page<InventoryBalanceResponse> listBalances(
            UUID organizationId, String actorEmail,
            UUID warehouseId, Pageable pageable) {

        authorizationService.requireViewAccess(actorEmail, organizationId);
        getWarehouse(organizationId, warehouseId);

        return balanceRepository.findByOrganizationIdAndWarehouseId(
                organizationId, warehouseId, pageable
        ).map(this::toBalanceResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryBalanceResponse> lowStock(
            UUID organizationId, String actorEmail,
            UUID warehouseId, Pageable pageable) {

        authorizationService.requireViewAccess(actorEmail, organizationId);

        Page<InventoryBalance> result;

        if (warehouseId != null) {
            getWarehouse(organizationId, warehouseId);

            result = balanceRepository.findLowStockByWarehouse(
                    organizationId, warehouseId, pageable
            );
        } else {
            result = balanceRepository.findLowStock(
                    organizationId, pageable
            );
        }

        return result.map(this::toBalanceResponse);
    }

    public InventoryBalanceResponse updateReorderLevel(
            UUID organizationId, String actorEmail,
            UUID warehouseId, UpdateReorderLevelRequest request) {

        authorizationService.requireInventoryManagement(actorEmail, organizationId);

        if (!warehouseId.equals(request.warehouseId())) {
            throw new IllegalArgumentException("Warehouse ID does not match the request.");
        }

        Warehouse warehouse = getWarehouse(organizationId, warehouseId);
        Product product = getProduct(organizationId, request.productId());

        InventoryBalance balance = getOrCreateBalance(
                organizationId, warehouse, product
        );

        balance.setReorderLevel(request.reorderLevel());

        return toBalanceResponse(balanceRepository.save(balance));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> movementHistory(
            UUID organizationId, String actorEmail,
            UUID productId, Pageable pageable) {

        authorizationService.requireViewAccess(actorEmail, organizationId);

        Page<StockMovement> movements = productId == null
                ? movementRepository.findByOrganizationId(organizationId, pageable)
                : movementRepository.findByOrganizationIdAndProductId(
                organizationId, productId, pageable
        );

        return movements.map(this::toMovementResponse);
    }

    private InventoryBalance getOrCreateBalance(
            UUID organizationId, Warehouse warehouse, Product product) {

        return balanceRepository.findForUpdate(
                organizationId, warehouse.getId(), product.getId()
        ).orElseGet(() -> {
            InventoryBalance balance = InventoryBalance.builder()
                    .organization(warehouse.getOrganization())
                    .warehouse(warehouse)
                    .product(product)
                    .quantityOnHand(BigDecimal.ZERO)
                    .reorderLevel(BigDecimal.ZERO)
                    .build();

            return balanceRepository.saveAndFlush(balance);
        });
    }

    private void subtractStock(InventoryBalance balance, BigDecimal quantity) {
        if (balance.getQuantityOnHand().compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient stock available.");
        }

        balance.setQuantityOnHand(
                balance.getQuantityOnHand().subtract(quantity)
        );
    }

    private StockMovementResponse recordMovement(
            UUID organizationId,
            Product product,
            Warehouse source,
            Warehouse destination,
            StockMovementType type,
            StockOperationRequest request,
            String actorEmail) {

        StockMovement movement = StockMovement.builder()
                .organization(destination != null
                        ? destination.getOrganization()
                        : source.getOrganization())
                .product(product)
                .sourceWarehouse(source)
                .destinationWarehouse(destination)
                .movementType(type)
                .quantity(request.quantity())
                .referenceNumber(request.referenceNumber())
                .reason(request.reason())
                .performedBy(getUser(actorEmail))
                .build();

        return toMovementResponse(movementRepository.save(movement));
    }

    private Warehouse getWarehouse(UUID organizationId, UUID warehouseId) {
        return warehouseRepository.findByIdAndOrganizationId(
                warehouseId, organizationId
        ).orElseThrow(() -> new IllegalArgumentException("Warehouse not found."));
    }

    private Warehouse getActiveWarehouse(UUID organizationId, UUID warehouseId) {
        Warehouse warehouse = getWarehouse(organizationId, warehouseId);

        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new IllegalStateException("This warehouse is inactive.");
        }

        return warehouse;
    }

    private Product getProduct(UUID organizationId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (!product.getOrganization().getId().equals(organizationId)) {
            throw new IllegalArgumentException(
                    "Product does not belong to this organization."
            );
        }

        return product;
    }

    private User getUser(String actorEmail) {
        return userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private InventoryBalanceResponse toBalanceResponse(InventoryBalance balance) {
        return new InventoryBalanceResponse(
                balance.getId(),
                balance.getWarehouse().getId(),
                balance.getWarehouse().getName(),
                balance.getProduct().getId(),
                balance.getProduct().getName(),
                balance.getProduct().getSku(),
                balance.getQuantityOnHand(),
                balance.getReorderLevel(),
                balance.getQuantityOnHand().compareTo(balance.getReorderLevel()) <= 0,
                balance.getUpdatedAt()
        );
    }

    private StockMovementResponse toMovementResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getSourceWarehouse() == null
                        ? null : movement.getSourceWarehouse().getId(),
                movement.getSourceWarehouse() == null
                        ? null : movement.getSourceWarehouse().getName(),
                movement.getDestinationWarehouse() == null
                        ? null : movement.getDestinationWarehouse().getId(),
                movement.getDestinationWarehouse() == null
                        ? null : movement.getDestinationWarehouse().getName(),
                movement.getQuantity(),
                movement.getReferenceNumber(),
                movement.getReason(),
                movement.getPerformedBy().getId(),
                movement.getCreatedAt()
        );
    }
}
package com.vertexone.prod.receiving.service;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.inventory.movement.dto.request.StockOperationRequest;
import com.vertexone.prod.inventory.balance.service.InventoryService;
import com.vertexone.prod.membership.enums.MembershipRole;
import com.vertexone.prod.membership.service.OrganizationAuthorizationService;
import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrder;
import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrderItem;
import com.vertexone.prod.procurement.purchaseorder.enums.PurchaseOrderStatus;
import com.vertexone.prod.procurement.purchaseorder.repository.PurchaseOrderItemRepository;
import com.vertexone.prod.procurement.purchaseorder.repository.PurchaseOrderRepository;
import com.vertexone.prod.receiving.dto.request.CreateGoodsReceiptRequest;
import com.vertexone.prod.receiving.dto.request.GoodsReceiptItemRequest;
import com.vertexone.prod.receiving.dto.response.GoodsReceiptResponse;
import com.vertexone.prod.receiving.entity.GoodsReceipt;
import com.vertexone.prod.receiving.entity.GoodsReceiptItem;
import com.vertexone.prod.receiving.repository.GoodsReceiptRepository;
import com.vertexone.prod.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryService inventoryService;
    private final OrganizationAuthorizationService authorizationService;
    private final UserRepository userRepository;


    public GoodsReceiptService(
            GoodsReceiptRepository goodsReceiptRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            InventoryService inventoryService,
            OrganizationAuthorizationService authorizationService,
            UserRepository userRepository
    ) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.inventoryService = inventoryService;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public GoodsReceiptResponse createReceipt(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail,
            CreateGoodsReceiptRequest request
    ) {
        var membership = authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        requireReceivingPermission(membership.getRole());

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndOrganizationIdForUpdate(
                        purchaseOrderId,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Purchase order not found.")
                );

        validatePurchaseOrder(purchaseOrder);

        if (purchaseOrder.getWarehouseId() == null) {
            throw new IllegalStateException(
                    "The purchase order does not have a warehouse assigned."
            );
        }

        User receiver = userRepository.findByEmail(actorEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );

        List<PurchaseOrderItem> purchaseOrderItems =
                purchaseOrderItemRepository.findByPurchaseOrderId(
                        purchaseOrder.getId()
                );
        if (purchaseOrderItems.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot receive goods for a purchase order without items."
            );
        }

        Map<UUID, PurchaseOrderItem> itemMap = purchaseOrderItems.stream()
                .collect(Collectors.toMap(
                        PurchaseOrderItem::getId,
                        Function.identity()
                ));

        Set<UUID> submittedItemIds = new HashSet<>();

        // Validate every line before changing inventory.
        for (GoodsReceiptItemRequest itemRequest : request.items()) {
            if (!submittedItemIds.add(itemRequest.purchaseOrderItemId())) {
                throw new IllegalArgumentException(
                        "Duplicate purchase order item in receipt."
                );
            }

            PurchaseOrderItem poItem = itemMap.get(
                    itemRequest.purchaseOrderItemId()
            );

            if (poItem == null) {
                throw new IllegalArgumentException(
                        "A receipt item does not belong to this purchase order."
                );
            }

            BigDecimal alreadyReceived = zeroIfNull(
                    poItem.getReceivedQuantity()
            );

            BigDecimal remaining = poItem.getOrderedQuantity()
                    .subtract(alreadyReceived);

            if (itemRequest.quantityReceived().compareTo(remaining) > 0) {
                throw new IllegalArgumentException(
                        "Received quantity exceeds the remaining ordered quantity "
                                + "for product " + poItem.getProduct().getName()
                );
            }

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "This purchase order item has already been fully received."
                );
            }
        }

        String receiptNumber = generateReceiptNumber();

        GoodsReceipt receipt = GoodsReceipt.builder()
                .organization(purchaseOrder.getOrganization())
                .purchaseOrder(purchaseOrder)
                .warehouseId(purchaseOrder.getWarehouseId())
                .receiptNumber(receiptNumber)
                .deliveryNoteNumber(request.deliveryNoteNumber())
                .receivedBy(receiver)
                .receivedAt(Instant.now())
                .notes(request.notes())
                .build();

        for (GoodsReceiptItemRequest itemRequest : request.items()) {
            PurchaseOrderItem poItem = itemMap.get(
                    itemRequest.purchaseOrderItemId()
            );

            Product product = poItem.getProduct();

            String referenceNumber = request.deliveryNoteNumber() != null
                    && !request.deliveryNoteNumber().isBlank()
                    ? request.deliveryNoteNumber()
                    : receiptNumber;

            String reason = "Goods received against purchase order "
                    + purchaseOrder.getPurchaseOrderNumber();

            // Reuse the existing inventory service.
            inventoryService.receive(
                    organizationId,
                    actorEmail,
                    new StockOperationRequest(
                            product.getId(),
                            purchaseOrder.getWarehouseId(),
                            itemRequest.quantityReceived(),
                            referenceNumber,
                            reason
                    )
            );

            BigDecimal updatedReceivedQuantity = zeroIfNull(
                    poItem.getReceivedQuantity()
            ).add(itemRequest.quantityReceived());

            poItem.setReceivedQuantity(updatedReceivedQuantity);

            GoodsReceiptItem receiptItem = GoodsReceiptItem.builder()
                    .purchaseOrderItem(poItem)
                    .product(product)
                    .quantityReceived(itemRequest.quantityReceived())
                    .notes(itemRequest.notes())
                    .build();

            receipt.addItem(receiptItem);
        }

        updatePurchaseOrderStatus(purchaseOrder, purchaseOrderItems);

        purchaseOrderRepository.save(purchaseOrder);

        GoodsReceipt savedReceipt = goodsReceiptRepository.save(receipt);

        return GoodsReceiptResponse.from(savedReceipt);
    }

    @Transactional(readOnly = true)
    public Page<GoodsReceiptResponse> getReceipts(
            UUID organizationId,
            String actorEmail,
            Pageable pageable
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        return goodsReceiptRepository
                .findByOrganizationIdOrderByReceivedAtDesc(
                        organizationId,
                        pageable
                )
                .map(GoodsReceiptResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<GoodsReceiptResponse> getReceiptsForPurchaseOrder(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail,
            Pageable pageable
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        purchaseOrderRepository.findByIdAndOrganizationId(
                purchaseOrderId,
                organizationId
        ).orElseThrow(() ->
                new IllegalArgumentException("Purchase order not found.")
        );

        return goodsReceiptRepository
                .findByOrganizationIdAndPurchaseOrderIdOrderByReceivedAtDesc(
                        organizationId,
                        purchaseOrderId,
                        pageable
                )
                .map(GoodsReceiptResponse::from);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptResponse getReceipt(
            UUID organizationId,
            UUID receiptId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        GoodsReceipt receipt = goodsReceiptRepository
                .findByIdAndOrganizationId(receiptId, organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Goods receipt not found.")
                );

        return GoodsReceiptResponse.from(receipt);
    }

    private void validatePurchaseOrder(PurchaseOrder purchaseOrder) {
        PurchaseOrderStatus status = purchaseOrder.getStatus();

        if (status != PurchaseOrderStatus.SENT
                && status != PurchaseOrderStatus.ACKNOWLEDGED
                && status != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException(
                    "Goods can only be received against a sent, acknowledged, "
                            + "or partially received purchase order."
            );
        }
    }

    private void updatePurchaseOrderStatus(
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderItem> items
    ) {
        boolean allReceived = items.stream().allMatch(item ->
                zeroIfNull(item.getReceivedQuantity())
                        .compareTo(item.getOrderedQuantity()) >= 0
        );

        boolean anyReceived = items.stream().anyMatch(item ->
                zeroIfNull(item.getReceivedQuantity())
                        .compareTo(BigDecimal.ZERO) > 0
        );

        if (allReceived) {
            purchaseOrder.setStatus(PurchaseOrderStatus.COMPLETED);
        } else if (anyReceived) {
            purchaseOrder.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
    }

    private void requireReceivingPermission(MembershipRole role) {
        if (role != MembershipRole.OWNER
                && role != MembershipRole.ADMIN
                && role != MembershipRole.INVENTORY_MANAGER) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to receive goods."
            );
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateReceiptNumber() {
        return "GR-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
    }
}
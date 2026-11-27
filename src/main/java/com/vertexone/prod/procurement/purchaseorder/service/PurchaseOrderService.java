package com.vertexone.prod.procurement.purchaseorder.service;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;
import com.vertexone.prod.product.entity.Product;
import com.vertexone.prod.product.repository.ProductRepository;
import com.vertexone.prod.procurement.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.vertexone.prod.procurement.purchaseorder.dto.request.PurchaseOrderItemRequest;
import com.vertexone.prod.procurement.purchaseorder.dto.response.PurchaseOrderResponse;
import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrder;
import com.vertexone.prod.procurement.purchaseorder.entity.PurchaseOrderItem;
import com.vertexone.prod.procurement.purchaseorder.enums.PurchaseOrderStatus;
import com.vertexone.prod.procurement.purchaseorder.repository.PurchaseOrderRepository;
import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisition;
import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisitionItem;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;
import com.vertexone.prod.procurement.requisition.repository.PurchaseRequisitionRepository;
import com.vertexone.prod.procurement.service.ProcurementAuthorizationService;
import com.vertexone.prod.vendor.entity.Vendor;
import com.vertexone.prod.vendor.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private static final int MONEY_SCALE = 4;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRequisitionRepository requisitionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final ProcurementAuthorizationService authorizationService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseRequisitionRepository requisitionRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            VendorRepository vendorRepository,
            ProcurementAuthorizationService authorizationService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.requisitionRepository = requisitionRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.vendorRepository = vendorRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public PurchaseOrderResponse create(
            UUID organizationId,
            String actorEmail,
            CreatePurchaseOrderRequest request
    ) {
        authorizationService.requireProcurementAccess(
                actorEmail, organizationId
        );

        Organization organization = getOrganization(organizationId);
        User actor = getUser(actorEmail);
        Vendor vendor = getVendor(organizationId, request.vendorId());

        if (request.expectedDeliveryDate() != null
                && request.expectedDeliveryDate().isBefore(request.orderDate())) {
            throw new IllegalArgumentException(
                    "Expected delivery date cannot be before the order date."
            );
        }

        PurchaseRequisition requisition = null;

        if (request.requisitionId() != null) {
            requisition = requisitionRepository
                    .findByIdAndOrganizationId(
                            request.requisitionId(), organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException("Purchase requisition not found.")
                    );

            if (requisition.getStatus() != RequisitionStatus.APPROVED) {
                throw new IllegalStateException(
                        "A purchase order can only be created from an approved requisition."
                );
            }
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .organization(organization)
                .purchaseOrderNumber(generatePurchaseOrderNumber())
                .requisitionId(
                requisition != null ? requisition.getId() : null
        )
                .vendor(vendor)
                .warehouseId(request.warehouseId())
                .status(PurchaseOrderStatus.DRAFT)
                .orderDate(request.orderDate())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .currencyCode(request.currencyCode().trim().toUpperCase(Locale.ROOT))
                .notes(request.notes())
                .createdBy(actor)
                .subtotal(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .taxTotal(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .grandTotal(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemRequest : request.items()) {
            Product product = getProduct(
                    organizationId, itemRequest.productId()
            );

            validateItemAgainstRequisition(
                    requisition, product.getId(), itemRequest.quantity()
            );

            PurchaseOrderItem item = buildItem(product, itemRequest);

            order.addItem(item);

            subtotal = subtotal.add(item.getLineSubtotal());
            taxTotal = taxTotal.add(item.getTaxAmount());
        }

        order.setSubtotal(round(subtotal));
        order.setTaxTotal(round(taxTotal));
        order.setGrandTotal(round(subtotal.add(taxTotal)));

        return PurchaseOrderResponse.from(
                purchaseOrderRepository.save(order)
        );
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> findAll(
            UUID organizationId,
            String actorEmail,
            PurchaseOrderStatus status,
            Pageable pageable
    ) {
        authorizationService.requireActiveMembership(
                actorEmail, organizationId
        );

        Page<PurchaseOrder> page = status == null
                ? purchaseOrderRepository.findByOrganizationId(
                organizationId, pageable
        )
                : purchaseOrderRepository.findByOrganizationIdAndStatus(
                organizationId, status, pageable
        );

        return page.map(PurchaseOrderResponse::from);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse findById(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(
                actorEmail, organizationId
        );

        return PurchaseOrderResponse.from(
                getPurchaseOrder(organizationId, purchaseOrderId)
        );
    }

    @Transactional
    public PurchaseOrderResponse submitForApproval(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireProcurementAccess(
                actorEmail, organizationId
        );

        PurchaseOrder order = getPurchaseOrder(
                organizationId, purchaseOrderId
        );

        requireStatus(order, PurchaseOrderStatus.DRAFT);

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "A purchase order must contain at least one item."
            );
        }

        order.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);

        return PurchaseOrderResponse.from(order);
    }

    @Transactional
    public PurchaseOrderResponse approve(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireApprovalAccess(
                actorEmail, organizationId
        );

        PurchaseOrder order = getPurchaseOrder(
                organizationId, purchaseOrderId
        );

        requireStatus(order, PurchaseOrderStatus.PENDING_APPROVAL);

        order.setStatus(PurchaseOrderStatus.APPROVED);
        order.setApprovedBy(getUser(actorEmail));
        order.setApprovedAt(Instant.now());

        return PurchaseOrderResponse.from(order);
    }

    @Transactional
    public PurchaseOrderResponse sendToVendor(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireProcurementAccess(
                actorEmail, organizationId
        );

        PurchaseOrder order = getPurchaseOrder(
                organizationId, purchaseOrderId
        );

        requireStatus(order, PurchaseOrderStatus.APPROVED);

        order.setStatus(PurchaseOrderStatus.SENT);
        order.setSentAt(Instant.now());

        return PurchaseOrderResponse.from(order);
    }

    @Transactional
    public PurchaseOrderResponse acknowledge(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireProcurementAccess(
                actorEmail, organizationId
        );

        PurchaseOrder order = getPurchaseOrder(
                organizationId, purchaseOrderId
        );

        requireStatus(order, PurchaseOrderStatus.SENT);

        order.setStatus(PurchaseOrderStatus.ACKNOWLEDGED);

        return PurchaseOrderResponse.from(order);
    }

    @Transactional
    public PurchaseOrderResponse cancel(
            UUID organizationId,
            UUID purchaseOrderId,
            String actorEmail
    ) {
        authorizationService.requireProcurementAccess(
                actorEmail, organizationId
        );

        PurchaseOrder order = getPurchaseOrder(
                organizationId, purchaseOrderId
        );

        if (order.getStatus() != PurchaseOrderStatus.DRAFT
                && order.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL
                && order.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "This purchase order cannot be cancelled in its current status."
            );
        }

        order.setStatus(PurchaseOrderStatus.CANCELLED);

        return PurchaseOrderResponse.from(order);
    }

    private PurchaseOrderItem buildItem(
            Product product,
            PurchaseOrderItemRequest request
    ) {
        BigDecimal quantity = request.quantity();
        BigDecimal unitPrice = round(request.unitPrice());
        BigDecimal taxRate = request.taxRate();

        BigDecimal lineSubtotal = round(quantity.multiply(unitPrice));

        BigDecimal taxAmount = round(
                lineSubtotal.multiply(taxRate)
                        .divide(BigDecimal.valueOf(100), MONEY_SCALE + 4,
                                RoundingMode.HALF_UP)
        );

        BigDecimal lineTotal = round(lineSubtotal.add(taxAmount));

        return PurchaseOrderItem.builder()
                .product(product)
                .orderedQuantity(quantity)
                .receivedQuantity(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .unitPrice(unitPrice)
                .taxRate(taxRate)
                .lineSubtotal(lineSubtotal)
                .taxAmount(taxAmount)
                .lineTotal(lineTotal)
                .notes(request.notes())
                .build();
    }

    private void validateItemAgainstRequisition(
            PurchaseRequisition requisition,
            UUID productId,
            BigDecimal orderedQuantity
    ) {
        if (requisition == null) {
            return;
        }

        PurchaseRequisitionItem approvedItem = requisition.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "The product is not part of the approved requisition."
                        )
                );

        if (approvedItem.getApprovedQuantity() == null
                || orderedQuantity.compareTo(
                approvedItem.getApprovedQuantity()
        ) > 0) {
            throw new IllegalArgumentException(
                    "Ordered quantity cannot exceed the approved requisition quantity."
            );
        }
    }

    private void requireStatus(
            PurchaseOrder order,
            PurchaseOrderStatus expectedStatus
    ) {
        if (order.getStatus() != expectedStatus) {
            throw new IllegalStateException(
                    "Purchase order must be in " + expectedStatus + " status."
            );
        }
    }

    private PurchaseOrder getPurchaseOrder(
            UUID organizationId,
            UUID purchaseOrderId
    ) {
        return purchaseOrderRepository.findByIdAndOrganizationId(
                purchaseOrderId, organizationId
        ).orElseThrow(() ->
                new IllegalArgumentException("Purchase order not found.")
        );
    }

    private Organization getOrganization(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found.")
                );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );
    }

    private Product getProduct(UUID organizationId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found.")
                );

        if (!product.getOrganization().getId().equals(organizationId)) {
            throw new IllegalArgumentException(
                    "The selected product does not belong to this organization."
            );
        }

        return product;
    }

    private Vendor getVendor(UUID organizationId, UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Vendor not found.")
                );

        if (!vendor.getOrganization().getId().equals(organizationId)) {
            throw new IllegalArgumentException(
                    "The selected vendor does not belong to this organization."
            );
        }

        return vendor;
    }

    private String generatePurchaseOrderNumber() {
        return "PO-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
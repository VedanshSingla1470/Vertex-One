package com.vertexone.prod.procurement.requisition.service;

import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.organization.repository.OrganizationRepository;
import com.vertexone.prod.product.entity.Product;
import com.vertexone.prod.product.repository.ProductRepository;
import com.vertexone.prod.procurement.requisition.dto.request.CreateRequisitionRequest;
import com.vertexone.prod.procurement.requisition.dto.request.RequisitionItemRequest;
import com.vertexone.prod.procurement.requisition.dto.response.RequisitionResponse;
import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisition;
import com.vertexone.prod.procurement.requisition.entity.PurchaseRequisitionItem;
import com.vertexone.prod.procurement.requisition.enums.RequisitionStatus;
import com.vertexone.prod.procurement.requisition.repository.PurchaseRequisitionRepository;
import com.vertexone.prod.procurement.service.ProcurementAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class PurchaseRequisitionService {

    private final PurchaseRequisitionRepository requisitionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProcurementAuthorizationService authorizationService;

    public PurchaseRequisitionService(
            PurchaseRequisitionRepository requisitionRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProcurementAuthorizationService authorizationService
    ) {
        this.requisitionRepository = requisitionRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public RequisitionResponse create(
            UUID organizationId,
            String actorEmail,
            CreateRequisitionRequest request
    ) {
        authorizationService.requireActiveMembership(actorEmail, organizationId);

        Organization organization = getOrganization(organizationId);
        User actor = getUser(actorEmail);

        PurchaseRequisition requisition = PurchaseRequisition.builder()
                .organization(organization)
                .requisitionNumber(generateRequisitionNumber(organizationId))
                .title(request.title().trim())
                .description(request.description())
                .status(RequisitionStatus.DRAFT)
                .requestedBy(actor)
                .build();

        for (RequisitionItemRequest itemRequest : request.items()) {
            Product product = getProduct(
                    organizationId,
                    itemRequest.productId()
            );

            PurchaseRequisitionItem item = PurchaseRequisitionItem.builder()
                    .product(product)
                    .requestedQuantity(itemRequest.requestedQuantity())
                    .notes(itemRequest.notes())
                    .build();

            requisition.addItem(item);
        }

        return RequisitionResponse.from(
                requisitionRepository.save(requisition)
        );
    }

    @Transactional(readOnly = true)
    public Page<RequisitionResponse> findAll(
            UUID organizationId,
            String actorEmail,
            RequisitionStatus status,
            Pageable pageable
    ) {
        authorizationService.requireActiveMembership(actorEmail, organizationId);

        Page<PurchaseRequisition> page = status == null
                ? requisitionRepository.findByOrganizationId(organizationId, pageable)
                : requisitionRepository.findByOrganizationIdAndStatus(
                organizationId, status, pageable
        );

        return page.map(RequisitionResponse::from);
    }

    @Transactional(readOnly = true)
    public RequisitionResponse findById(
            UUID organizationId,
            UUID requisitionId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(actorEmail, organizationId);

        return RequisitionResponse.from(
                getRequisition(organizationId, requisitionId)
        );
    }

    @Transactional
    public RequisitionResponse submit(
            UUID organizationId,
            UUID requisitionId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(actorEmail, organizationId);

        PurchaseRequisition requisition = getRequisition(
                organizationId, requisitionId
        );

        if (requisition.getStatus() != RequisitionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft requisitions can be submitted."
            );
        }

        if (requisition.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "A requisition must contain at least one item."
            );
        }

        requisition.setStatus(RequisitionStatus.SUBMITTED);

        return RequisitionResponse.from(requisition);
    }

    @Transactional
    public RequisitionResponse approve(
            UUID organizationId,
            UUID requisitionId,
            String actorEmail
    ) {
        authorizationService.requireApprovalAccess(actorEmail, organizationId);

        PurchaseRequisition requisition = getRequisition(
                organizationId, requisitionId
        );

        if (requisition.getStatus() != RequisitionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only submitted requisitions can be approved."
            );
        }

        for (PurchaseRequisitionItem item : requisition.getItems()) {
            if (item.getApprovedQuantity() == null) {
                item.setApprovedQuantity(item.getRequestedQuantity());
            }

            validateApprovedQuantity(item);
        }

        requisition.setStatus(RequisitionStatus.APPROVED);
        requisition.setReviewedBy(getUser(actorEmail));
        requisition.setReviewedAt(Instant.now());
        requisition.setRejectionReason(null);

        return RequisitionResponse.from(requisition);
    }

    @Transactional
    public RequisitionResponse reject(
            UUID organizationId,
            UUID requisitionId,
            String actorEmail,
            String reason
    ) {
        authorizationService.requireApprovalAccess(actorEmail, organizationId);

        PurchaseRequisition requisition = getRequisition(
                organizationId, requisitionId
        );

        if (requisition.getStatus() != RequisitionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only submitted requisitions can be rejected."
            );
        }

        requisition.setStatus(RequisitionStatus.REJECTED);
        requisition.setReviewedBy(getUser(actorEmail));
        requisition.setReviewedAt(Instant.now());
        requisition.setRejectionReason(reason.trim());

        return RequisitionResponse.from(requisition);
    }

    @Transactional
    public RequisitionResponse cancel(
            UUID organizationId,
            UUID requisitionId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(actorEmail, organizationId);

        PurchaseRequisition requisition = getRequisition(
                organizationId, requisitionId
        );

        if (requisition.getStatus() != RequisitionStatus.DRAFT
                && requisition.getStatus() != RequisitionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only draft or submitted requisitions can be cancelled."
            );
        }

        requisition.setStatus(RequisitionStatus.CANCELLED);

        return RequisitionResponse.from(requisition);
    }

    private void validateApprovedQuantity(PurchaseRequisitionItem item) {
        BigDecimal approved = item.getApprovedQuantity();

        if (approved == null
                || approved.compareTo(BigDecimal.ZERO) <= 0
                || approved.compareTo(item.getRequestedQuantity()) > 0) {
            throw new IllegalArgumentException(
                    "Approved quantity must be greater than zero and cannot exceed requested quantity."
            );
        }
    }

    private PurchaseRequisition getRequisition(
            UUID organizationId,
            UUID requisitionId
    ) {
        return requisitionRepository.findByIdAndOrganizationId(
                requisitionId, organizationId
        ).orElseThrow(() ->
                new IllegalArgumentException("Purchase requisition not found.")
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

    private String generateRequisitionNumber(UUID organizationId) {
        return "REQ-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
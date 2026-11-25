
package com.vertexone.prod.product.service;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.product.category.entity.ProductCategory;
import com.vertexone.prod.product.category.repository.ProductCategoryRepository;
import com.vertexone.prod.product.dto.request.ProductCreateRequest;
import com.vertexone.prod.product.dto.request.ProductUpdateRequest;
import com.vertexone.prod.product.dto.response.ProductResponse;
import com.vertexone.prod.product.dto.request.ProductStatusUpdateRequest;
import com.vertexone.prod.product.entity.Product;
import com.vertexone.prod.product.enums.ProductStatus;
import com.vertexone.prod.product.repository.ProductRepository;
import com.vertexone.prod.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductAuthorizationService authorizationService;

    @Transactional
    public ProductResponse createProduct(
            UUID organizationId,
            ProductCreateRequest request,
            String actorEmail
    ) {
        Membership membership = authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        String sku = normalizeSku(request.sku());

        if (productRepository.existsByOrganizationIdAndSku(
                organizationId,
                sku
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with this SKU already exists"
            );
        }

        ProductCategory category = resolveCategory(
                organizationId,
                request.categoryId()
        );

        Product product = Product.builder()
                .organization(membership.getOrganization())
                .category(category)
                .sku(sku)
                .name(request.name().trim())
                .description(request.description())
                .unitOfMeasure(request.unitOfMeasure())
                .status(ProductStatus.ACTIVE)
                .build();

        return toResponse(productRepository.save(product));
    }

    public Page<ProductResponse> searchProducts(
            UUID organizationId,
            String keyword,
            ProductStatus status,
            UUID categoryId,
            Pageable pageable,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        if (categoryId != null) {
            getCategoryOrThrow(categoryId, organizationId);
        }

        String normalizedKeyword = keyword == null || keyword.isBlank()
                ? null
                : keyword.trim();

        return productRepository.searchProducts(
                organizationId,
                normalizedKeyword,
                status,
                categoryId,
                pageable
        ).map(this::toResponse);
    }

    public ProductResponse getProduct(
            UUID organizationId,
            UUID productId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        return toResponse(
                getProductOrThrow(productId, organizationId)
        );
    }

    @Transactional
    public ProductResponse updateProduct(
            UUID organizationId,
            UUID productId,
            ProductUpdateRequest request,
            String actorEmail
    ) {
        authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        Product product = getProductOrThrow(
                productId,
                organizationId
        );

        String sku = normalizeSku(request.sku());

        if (productRepository.existsByOrganizationIdAndSkuAndIdNot(
                organizationId,
                sku,
                productId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another product with this SKU already exists"
            );
        }

        ProductCategory category = resolveCategory(
                organizationId,
                request.categoryId()
        );

        product.setCategory(category);
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setUnitOfMeasure(request.unitOfMeasure());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProductStatus(
            UUID organizationId,
            UUID productId,
            ProductStatusUpdateRequest request,
            String actorEmail
    ) {
        authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        Product product = getProductOrThrow(
                productId,
                organizationId
        );

        product.setStatus(request.status());

        return toResponse(productRepository.save(product));
    }

    private Product getProductOrThrow(
            UUID productId,
            UUID organizationId
    ) {
        return productRepository
                .findByIdAndOrganizationId(productId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"
                ));
    }

    private ProductCategory resolveCategory(
            UUID organizationId,
            UUID categoryId
    ) {
        if (categoryId == null) {
            return null;
        }

        return getCategoryOrThrow(categoryId, organizationId);
    }

    private ProductCategory getCategoryOrThrow(
            UUID categoryId,
            UUID organizationId
    ) {
        return categoryRepository
                .findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product category not found"
                ));
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getOrganization().getId(),
                product.getCategory() != null
                        ? product.getCategory().getId()
                        : null,
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitOfMeasure(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

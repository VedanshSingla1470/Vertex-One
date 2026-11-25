
package com.vertexone.prod.product.category.service;

import com.vertexone.prod.membership.entity.Membership;
import com.vertexone.prod.organization.entity.Organization;
import com.vertexone.prod.product.category.dto.request.ProductCategoryCreateRequest;
import com.vertexone.prod.product.category.dto.request.ProductCategoryUpdateRequest;
import com.vertexone.prod.product.category.dto.response.ProductCategoryResponse;
import com.vertexone.prod.product.category.entity.ProductCategory;
import com.vertexone.prod.product.category.repository.ProductCategoryRepository;
import com.vertexone.prod.product.repository.ProductRepository;
import com.vertexone.prod.product.service.ProductAuthorizationService;
import com.vertexone.prod.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductAuthorizationService authorizationService;

    @Transactional
    public ProductCategoryResponse createCategory(
            UUID organizationId,
            ProductCategoryCreateRequest request,
            String actorEmail
    ) {
        Membership membership = authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        String name = request.name().trim();

        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                name
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product category with this name already exists"
            );
        }

        ProductCategory category = ProductCategory.builder()
                .organization(membership.getOrganization())
                .name(name)
                .description(request.description())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    public List<ProductCategoryResponse> getCategories(
            UUID organizationId,
            String actorEmail
    ) {
        authorizationService.requireActiveMembership(
                actorEmail,
                organizationId
        );

        return categoryRepository
                .findAllByOrganizationIdOrderByNameAsc(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductCategoryResponse updateCategory(
            UUID organizationId,
            UUID categoryId,
            ProductCategoryUpdateRequest request,
            String actorEmail
    ) {
        authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        ProductCategory category = getCategoryOrThrow(
                categoryId,
                organizationId
        );

        String name = request.name().trim();

        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
                organizationId,
                name,
                categoryId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product category with this name already exists"
            );
        }

        category.setName(name);
        category.setDescription(request.description());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(
            UUID organizationId,
            UUID categoryId,
            String actorEmail
    ) {
        authorizationService.requireCatalogManager(
                actorEmail,
                organizationId
        );

        ProductCategory category = getCategoryOrThrow(
                categoryId,
                organizationId
        );

        if (productRepository.existsByOrganizationIdAndCategoryId(
                organizationId,
                categoryId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a category assigned to products"
            );
        }

        categoryRepository.delete(category);
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

    private ProductCategoryResponse toResponse(
            ProductCategory category
    ) {
        return new ProductCategoryResponse(
                category.getId(),
                category.getOrganization().getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

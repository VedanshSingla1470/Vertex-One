package com.vertexone.prod.product.category.controller;

import com.vertexone.prod.product.category.dto.request.ProductCategoryCreateRequest;
import com.vertexone.prod.product.category.dto.request.ProductCategoryUpdateRequest;
import com.vertexone.prod.product.category.dto.response.ProductCategoryResponse;
import com.vertexone.prod.product.category.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    public ResponseEntity<ProductCategoryResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody ProductCategoryCreateRequest request,
            Authentication authentication
    ) {
        ProductCategoryResponse response = categoryService.createCategory(
                organizationId,
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductCategoryResponse>> getAll(
            @PathVariable UUID organizationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                categoryService.getCategories(
                        organizationId,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ProductCategoryResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody ProductCategoryUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                categoryService.updateCategory(
                        organizationId,
                        categoryId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId,
            Authentication authentication
    ) {
        categoryService.deleteCategory(
                organizationId,
                categoryId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}

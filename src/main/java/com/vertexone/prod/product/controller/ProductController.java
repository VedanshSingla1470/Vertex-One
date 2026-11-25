package com.vertexone.prod.product.controller;

import com.vertexone.prod.product.dto.request.ProductCreateRequest;
import com.vertexone.prod.product.dto.request.ProductUpdateRequest;
import com.vertexone.prod.product.dto.request.ProductStatusUpdateRequest;
import com.vertexone.prod.product.dto.response.ProductResponse;
import com.vertexone.prod.product.enums.ProductStatus;
import com.vertexone.prod.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication
    ) {
        ProductResponse response = productService.createProduct(
                organizationId,
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> search(
            @PathVariable UUID organizationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) UUID categoryId,
            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(
                        organizationId,
                        keyword,
                        status,
                        categoryId,
                        pageable,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                productService.getProduct(
                        organizationId,
                        productId,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                productService.updateProduct(
                        organizationId,
                        productId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ProductResponse> updateStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                productService.updateProductStatus(
                        organizationId,
                        productId,
                        request,
                        authentication.getName()
                )
        );
    }
}

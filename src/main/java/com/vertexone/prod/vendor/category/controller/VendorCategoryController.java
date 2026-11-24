package com.vertexone.prod.vendor.category.controller;

import com.vertexone.prod.vendor.category.dto.request.VendorCategoryCreateRequest;
import com.vertexone.prod.vendor.category.dto.response.VendorCategoryResponse;
import com.vertexone.prod.vendor.category.service.VendorCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/vendor-categories")
public class VendorCategoryController {

    private final VendorCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorCategoryResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody VendorCategoryCreateRequest request,
            Authentication authentication) {

        return categoryService.createCategory(
                organizationId, request, authentication.getName());
    }

    @GetMapping
    public List<VendorCategoryResponse> getAll(
            @PathVariable UUID organizationId,
            Authentication authentication) {

        return categoryService.getCategories(
                organizationId, authentication.getName());
    }

    @PutMapping("/{categoryId}")
    public VendorCategoryResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody VendorCategoryCreateRequest request,
            Authentication authentication) {

        return categoryService.updateCategory(
                organizationId, categoryId, request, authentication.getName());
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId,
            Authentication authentication) {

        categoryService.deleteCategory(
                organizationId, categoryId, authentication.getName());
    }
}
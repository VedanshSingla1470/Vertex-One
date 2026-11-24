package com.vertexone.prod.vendor.contact.repository;

import com.vertexone.prod.vendor.contact.entity.VendorContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorContactRepository
        extends JpaRepository<VendorContact, UUID> {

    List<VendorContact> findByVendorId(UUID vendorId);

    Optional<VendorContact> findByIdAndVendorId(
            UUID id,
            UUID vendorId
    );

    Optional<VendorContact> findByVendorIdAndPrimaryTrue(
            UUID vendorId
    );
}
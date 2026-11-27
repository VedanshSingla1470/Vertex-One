package com.vertexone.prod.receiving.repository;

import com.vertexone.prod.receiving.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptItemRepository
        extends JpaRepository<GoodsReceiptItem, UUID> {

    List<GoodsReceiptItem> findByGoodsReceiptId(UUID goodsReceiptId);
}
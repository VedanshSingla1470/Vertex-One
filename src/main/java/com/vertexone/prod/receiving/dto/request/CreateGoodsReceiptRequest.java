package com.vertexone.prod.receiving.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGoodsReceiptRequest(

        @Size(max = 100)
        String deliveryNoteNumber,

        @Size(max = 2000)
        String notes,

        @NotEmpty
        List<@Valid GoodsReceiptItemRequest> items

) {
}
package org.beyond.ordersystem.ordering.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductUpdateStockDto {
    private Long productId;
    private Integer quantity;
}

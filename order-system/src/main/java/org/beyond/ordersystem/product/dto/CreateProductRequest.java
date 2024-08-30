package org.beyond.ordersystem.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.product.domain.Product;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private MultipartFile productImage;

    public static Product toEntity(CreateProductRequest createProductRequest) {
        return Product.builder()
                .name(createProductRequest.getName())
                .category(createProductRequest.getCategory())
                .price(createProductRequest.getPrice())
                .stockQuantity(createProductRequest.getStockQuantity())
                .build();
    }
}

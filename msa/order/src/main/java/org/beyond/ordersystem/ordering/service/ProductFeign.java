package org.beyond.ordersystem.ordering.service;

import com.sun.net.httpserver.Authenticator;
import org.beyond.ordersystem.common.config.FeignConfig;
import org.beyond.ordersystem.common.dto.SuccessResponse;
import org.beyond.ordersystem.ordering.dto.product.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 24.09.01 kube 배포: url 설정을 추가하여 service 자원을 검색하도록 설정한다.
// url="http://product-service" => 쿠버네티스 서비스 자원의 이름을 의미한다.
// url="" 이부분을 추가해주었다
@FeignClient(name="product-service", url="http://product-service", configuration = FeignConfig.class)
public interface ProductFeign {
    @GetMapping(value="/product/{id}")
    SuccessResponse getProductById(@PathVariable("id") Long id);

    @PutMapping(value="/product/update-stock")
    void updateProductStock(@RequestBody ProductUpdateStockDto dto);
}

package org.beyond.ordersystem.ordering.controller;

import lombok.RequiredArgsConstructor;
import org.beyond.ordersystem.common.dto.SuccessResponse;
import org.beyond.ordersystem.ordering.domain.Ordering;
import org.beyond.ordersystem.ordering.dto.CreateOrderDetailResponse;
import org.beyond.ordersystem.ordering.dto.CreateOrderRequest;
import org.beyond.ordersystem.ordering.dto.CreateOrderResponse;
import org.beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class OrderingController {
    private final OrderingService orderingService;

    @PostMapping("/order/create")
    public ResponseEntity<SuccessResponse> createOrder(@RequestBody List<CreateOrderRequest> createOrderRequest) {
        CreateOrderResponse createOrderResponse = orderingService.createOrder(createOrderRequest);

        SuccessResponse response = SuccessResponse.builder()
                .httpStatus(HttpStatus.CREATED)
                .result(createOrderResponse)
                .statusMessage("주문이 등록되었습니다.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 관리자 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/list")
    public ResponseEntity<SuccessResponse> productList(@PageableDefault(size = 10) Pageable pageable) {
        Page<CreateOrderResponse> orderList = orderingService.orderList(pageable);
        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("주문 리스트입니다.")
                .httpStatus(HttpStatus.OK)
                .result(orderList)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 내 주문만 볼 수 있는 myOrders: order/myorders
    @GetMapping("/order/myorders")
    public ResponseEntity<SuccessResponse> getMyOrders(@PageableDefault(size = 10) Pageable pageable) {
        Page<CreateOrderResponse> myOrderList = orderingService.myOrderList(pageable);

        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("주문 리스트입니다.")
                .httpStatus(HttpStatus.OK)
                .result(myOrderList)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/order/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        Ordering ordering = orderingService.cancelOrder(orderId);
        SuccessResponse successResponse = new SuccessResponse(HttpStatus.OK, "정상 취소", ordering.getId());
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

}

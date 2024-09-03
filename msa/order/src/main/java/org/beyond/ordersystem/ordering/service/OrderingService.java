package org.beyond.ordersystem.ordering.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beyond.ordersystem.common.auth.SecurityUtil;
import org.beyond.ordersystem.common.dto.SuccessResponse;
import org.beyond.ordersystem.common.service.StockInventoryService;

import org.beyond.ordersystem.ordering.controller.SseController;
import org.beyond.ordersystem.ordering.domain.OrderDetail;
import org.beyond.ordersystem.ordering.domain.OrderStatus;
import org.beyond.ordersystem.ordering.domain.Ordering;
import org.beyond.ordersystem.ordering.dto.*;
import org.beyond.ordersystem.ordering.dto.product.ProductDto;
import org.beyond.ordersystem.ordering.dto.product.ProductUpdateStockDto;
import org.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import org.beyond.ordersystem.ordering.repository.OrderingRepository;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.sql.PreparedStatement;
import java.util.List;

import static org.beyond.ordersystem.ordering.domain.OrderStatus.CANCELED;

@Slf4j
//@Transactional(propagation = Propagation.REQUIRES_NEW) // 전파레벨 새로운 트랜잭션 생성하도록 바꿈
@RequiredArgsConstructor
@Service
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SecurityUtil securityUtil;
    private final StockInventoryService stockInventoryService;
    private final EntityManager em; // em DI 받기
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
//    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void createOrderDetailItem(CreateOrderRequest orderDto, Ordering ordering) {
        int quantity = orderDto.getQuantity();
        OrderDetail orderDetail =  OrderDetail.builder()
//                .product(product)
                .quantity(quantity)
                .ordering(ordering)
                .build();

        orderDetailRepository.save(orderDetail);

    }

    // 내가 차감하고자 하는 개수보다 재고가 작으면 예외 던지기
    // 정상적으로 차감 가능하면 재고감소 업데이트

    @Transactional
    public CreateOrderResponse restTemplateCreateOrder(List<CreateOrderRequest> createOrderRequest) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        Ordering ordering = orderingRepository.save(CreateOrderRequest.toEntity(email)); // 원래 멤버였는데 이메일로 바꿈
         // ⭐️ 나중에 주석 해제할 부분
        for (CreateOrderRequest orderDto : createOrderRequest) {

            String productGetUrl = "http://product-service/product/" + orderDto.getProductId();
            HttpHeaders httpHeaders = new HttpHeaders();
            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials(); // ⭐️ credentials 자리에 세팅된 토큰을 꺼냄
            httpHeaders.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            // RestTemplate의 리턴 타입은 무조건 ResponseEntity 형식이다.
            ResponseEntity<SuccessResponse> productEntity = restTemplate.exchange(productGetUrl, HttpMethod.GET, entity, SuccessResponse.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(productEntity.getBody().getResult(), ProductDto.class);

            //== 재고 확인/감소 로직 ==//
            // 변경감지(=dirty checking)으로 인해 별도의 save 불필요
            // ⭐️ RestTemplate을 통한 update 요청이 필요하다!
//            decreaseStock(product, orderDto.getQuantity());
            //== 재고 확인/감소 로직 끝==//

            //== MSA 재고 감소 로직 ==//
            String updateUrl = "http://product-service/product/update-stock";
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(new ProductUpdateStockDto(orderDto.getProductId(), orderDto.getQuantity()), httpHeaders);
            restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Void.class);

            OrderDetail orderDetail = CreateOrderDetailRequest.toEntity(orderDto, productDto.getId(), ordering);
            OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);// 이거 안해줘도 되네. cascade PERSIST 되나보다 => 이제 이거 해줘야 Exception 안뜨네 왜지..
            ordering.getOrderDetails().add(savedOrderDetail);
        }

        // 마지막에 저장~ => 이렇게 해야 디테일 아이디가 제대로 내려옴 => 이때 detail이 cascade persist가 되나봄
        Ordering savedOrder = orderingRepository.save(ordering);
        sseController.publishMessage(CreateOrderResponse.fromEntity(savedOrder), "admin@test.com");
        // 위에서 저장하면 null로 내려오는 이유가. 쿼리가 안나가서 그럼. 아니 왜 쿼리가 안나가냐

        return CreateOrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    public CreateOrderResponse feignClientCreateOrder(List<CreateOrderRequest> createOrderRequest) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        Ordering ordering = orderingRepository.save(CreateOrderRequest.toEntity(email)); // 원래 멤버였는데 이메일로 바꿈

        for (CreateOrderRequest orderDto : createOrderRequest) {
            // ResponseEntity가 기본 응답 값이므로, 바로 CommonResDto로 매핑
            SuccessResponse successResponse = productFeign.getProductById(orderDto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(successResponse.getResult(), ProductDto.class);

            // 재고 업데이트
            productFeign.updateProductStock(new ProductUpdateStockDto(orderDto.getProductId(), orderDto.getQuantity()));

            OrderDetail orderDetail = CreateOrderDetailRequest.toEntity(orderDto, productDto.getId(), ordering);
            OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);// 이거 안해줘도 되네. cascade PERSIST 되나보다 => 이제 이거 해줘야 Exception 안뜨네 왜지..
            ordering.getOrderDetails().add(savedOrderDetail);
        }

        Ordering savedOrder = orderingRepository.save(ordering);
        sseController.publishMessage(CreateOrderResponse.fromEntity(savedOrder), "admin@test.com");

        return CreateOrderResponse.fromEntity(savedOrder);
    }

    //== Kafka 코드 ==//
//    @Transactional
//    public CreateOrderResponse orderFeignKafkaCreateOrder(List<CreateOrderRequest> createOrderRequest) {
//        String email = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//        Ordering ordering = orderingRepository.save(CreateOrderRequest.toEntity(email)); // 원래 멤버였는데 이메일로 바꿈
//
//        for (CreateOrderRequest orderDto : createOrderRequest) {
//            // ResponseEntity가 기본 응답 값이므로, 바로 CommonResDto로 매핑
//            SuccessResponse successResponse = productFeign.getProductById(orderDto.getProductId());
//            ObjectMapper objectMapper = new ObjectMapper();
//            ProductDto productDto = objectMapper.convertValue(successResponse.getResult(), ProductDto.class);
//
//            // 재고 업데이트: 동기 요청 => 비동기로 변경
//            ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(orderDto.getProductId(), orderDto.getQuantity());
//            kafkaTemplate.send("product-update-topic", productUpdateStockDto);
//
//            OrderDetail orderDetail = CreateOrderDetailRequest.toEntity(orderDto, productDto.getId(), ordering);
//            OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);// 이거 안해줘도 되네. cascade PERSIST 되나보다 => 이제 이거 해줘야 Exception 안뜨네 왜지..
//            ordering.getOrderDetails().add(savedOrderDetail);
//        }
//
//        Ordering savedOrder = orderingRepository.save(ordering);
//        sseController.publishMessage(CreateOrderResponse.fromEntity(savedOrder), "admin@test.com");
//
//        return CreateOrderResponse.fromEntity(savedOrder);
//    }



    public Page<CreateOrderResponse> orderList(Pageable pageable) {
        Page<Ordering> orderList = orderingRepository.findAll(pageable);


        return orderList.map(CreateOrderResponse::fromEntity);
    }

    // ⭐️ 나중에 주석 해제할 부분
//    @Transactional
//    private void decreaseStock(Product product, int count) {
//        //== 이 부분에서 redis를 통해서 재고 관리를 하겠다. ==//
//        // 상품명에 sale이라는 키워드가 붙어있는 경우에만 동시성 처리해보자
//
//        // product api에 요청을 통해 product 객체를 조회해야함
//
//        if(product.getName().contains("sale")) {
//            // 레디스가 음수로 내려갈 경우 추후 재고 update 상황에서 increase값이 정확하지 않을 수 있으므로,
//            // 음수면 0으로 setting 로직이 필요
////            int newQuantity = ;
//            if((int) stockInventoryService.decreaseStock(product.getId(), count) < 0) {
//                stockInventoryService.setStockZero(product.getId()); // 여기서 자체적 롤백 처리
//                throw new IllegalArgumentException("재고 부족");
//            }
//
//            // RDB에 재고를 업데이트
//            // 해결책: 이벤트 기반의 아키텍처 구상! => 이벤트 드리븐
//            // rabbitmq를 통해 비동기적으로 이벤트 처리
//            stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), count));
//        } else {
//            if(product.getStockQuantity() < count) {
//                throw new IllegalArgumentException("재고가 부족합니다.");
//            }
//
//            product.decreaseStockQuantity(count);
//        }
//    }

    @Transactional(readOnly = true)
    public Page<CreateOrderResponse> myOrderList(Pageable pageable) {
        String email = securityUtil.getEmailFromSecurityContext();

        Page<Ordering> orderList = orderingRepository
                .findAllByMemberEmail(pageable, email);

        return orderList.map(CreateOrderResponse::fromEntity);

    }

    @Transactional
    public Ordering cancelOrder(Long orderId) {
//        String email = securityUtil.getEmailFromSecurityContext();
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("없음"));

        Ordering order = orderingRepository.findByIdOrThrow(orderId);

//        if(!order.getMember().getId().equals(member.getId())) {
//            throw new IllegalArgumentException("주문자만 취소 가능합니다.");
//        }

        order.updateStatus(CANCELED);

        return order;
    }


//    public KafkaTemplate<String, Object> getKafkaTemplate() {
//        return kafkaTemplate;
//    }
}

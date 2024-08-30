//package org.beyond.ordersystem.ordering.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.beyond.ordersystem.common.config.RabbitMqConfig;
//import org.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
//import org.beyond.ordersystem.product.domain.Product;
//import org.beyond.ordersystem.product.repository.ProductRepository;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class StockDecreaseEventHandler {
//
//    private final RabbitTemplate rabbitTemplate;
//    private final RabbitMqConfig rabbitMqConfig;
//    private final ProductRepository productRepository;
//
//
//    public void publish(StockDecreaseEvent event) {
//        // convertAndSend: => 이걸로 큐로 메시지를 보내는 건가보네
//        /* rabbitTemplate.convertAndSend(큐이름, 자바객체(json)이 들어옴); */
//        rabbitTemplate.convertAndSend(/*큐이름*/RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
//    // 동기. 이부분에서는 경합 이슈가 발생하지 않는다.
//    // 트랜잭션이 완료된 이후에 그 다음 메시지를 수신하므로 동시성 이슈 발생 X
//    @Transactional // transactional은 @Component가 붙어있는 곳에 붙을 수 있다.
//    @RabbitListener(queues = /* 큐 이름 */ RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen(Message message) {
//        String messageBody = new String(message.getBody()/*byte[]*/);
//        log.info("큐 메시지: {}", messageBody);
//
//        // json 메시지를 ObjectMapper로 직접 parsing -> StockDecrease
//        StockDecreaseEvent stockDecreaseEvent;
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
//        } catch (JsonProcessingException e) {
//            // 여기서 예외가 터지면 다시 큐에 넣어주어야한다.(그냥 알아서 들어가는거 같기는 한데, 테스트가 필요하다.)
//            throw new IllegalArgumentException("잘못된 메시지입니다.");
//        }
//
//        // 재고 update
//        Product product = productRepository.findByIdOrThrow(stockDecreaseEvent.getProductId());
//        product.decreaseStockQuantity(stockDecreaseEvent.getProductCount());
//    }
//}

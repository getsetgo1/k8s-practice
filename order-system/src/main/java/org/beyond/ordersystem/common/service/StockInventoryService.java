package org.beyond.ordersystem.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

//== 동시성 ==//
@Service
public class StockInventoryService {
    @Qualifier("3")
    private final RedisTemplate<String, String> redisTemplate;

    public StockInventoryService(@Qualifier("3") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 상품 등록 시 increaseStock 호출
    public long increaseStock(Long itemId, int quantity) {
        // 아래 메서드의 리턴 값은 잔량(남은 재고)
        return redisTemplate.opsForValue().increment(String.valueOf(itemId), quantity);
    }

    // 주문 등록 시 decreaseStock 호출
    public long decreaseStock(Long itemId, int quantity) {
        // 아래 메서드의 리턴 값은 잔량(남은 재고)
        // 서드파티 작업할 때 형변환이 너무 번거롭다잉
//        int remains = Integer.parseInt(redisTemplate.opsForValue().get(String.valueOf(itemId)));
//        if(remains < quantity) {
//            return -1L;
//        } else {
        return redisTemplate.opsForValue().decrement(String.valueOf(itemId), quantity);
    }

    public void setStockZero(Long itemId) {
        redisTemplate.opsForValue().set(String.valueOf(itemId), "0");
    }


}

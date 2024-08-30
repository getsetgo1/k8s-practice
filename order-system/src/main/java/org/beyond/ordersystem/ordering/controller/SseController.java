package org.beyond.ordersystem.ordering.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.beyond.ordersystem.ordering.dto.CreateOrderResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@RestController
public class SseController implements MessageListener {
    // SseEmitter는 연결된 사용자 정보를 의미
    // ConcurrentHashMap은 Thread-safe한 Map이다. == 동시성 이슈 발생 안함
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 여러번 구독 방지하기 위한 hash set
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();
    @Qualifier("4")
    private final RedisTemplate<String, Object> sseRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;


    public SseController(@Qualifier("4") RedisTemplate<String, Object> sseRedisTemplate, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.sseRedisTemplate = sseRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(14400*60*1000L);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        emitters.put(email, emitter);
        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));

        try {
            // "connect": 이벤트 이름
            // "connected!!" : 메시지 내용
            emitter.send(SseEmitter.event().name("connect").data("connected!!"));
        } catch(IOException e) {
            e.printStackTrace();
        }


        subscribeChannel(email); // ⭐️ 프론트엔드와 서버가 커넥션을 맺을 때 => 레디스 채널도 subscribe함

        return emitter;
    }

    public void publishMessage(CreateOrderResponse createOrderResponse, String email) {
        SseEmitter emitter = emitters.get(email);

//        if(emitter != null) { // 내 서버가 가지고 있으면? => 바로 전송
//            try {
//                emitter.send(SseEmitter.event().name("ordered").data(createOrderResponse));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
            sseRedisTemplate.convertAndSend(email, createOrderResponse);
//        }
    }

    @Override // 이곳에서 A에게 알림 전달
    public void onMessage(Message message, byte[] pattern) { // pattern에 키값이 들어가 있음
        // message 내용 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CreateOrderResponse dto = objectMapper.readValue(message.getBody(), CreateOrderResponse.class);
            System.out.println("[line 74]: listening");
            System.out.println(dto);

            String email = new String(pattern, StandardCharsets.UTF_8);
            SseEmitter emitter = emitters.get(email);

            if(emitter != null) {
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // listen을 하자마자 onMessage 메서드를 실행한다.
    private MessageListenerAdapter createListenerAdapter(SseController sseController) {
        return new MessageListenerAdapter(sseController, "onMessage");
    }

    // 이메일에 해당 되는 메시지를 listen하는 listener를 추가하는 것이다.
    // 이메일에 해당돠는 메시지를 listen하는 listener를 추가한 것
    public void subscribeChannel(String email) {
        log.info("[line 110]: {}", subscribeList);

        // sseController를 넣는 것
        // 이미 구독한 email일 경우, 더이상 구독 안하는 분기처리
        if(!subscribeList.contains(email)) {
            MessageListenerAdapter listenerAdapter = createListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }


}

package org.beyond.ordersystem.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.beyond.ordersystem.ordering.controller.SseController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // application.yml의 spring.redis.host의 정보를 소스코드의 변수로 가져오는 것이다.
    @Value("${spring.redis.host}") // org.springframework.beans.factory.annotation.Value;
    public String host;

    @Value("${spring.redis.port}")
    public Integer port;



    // RedisConnectionFactory는 Redis 서버와의 연결을 설정하는 역할
    // LettuceConnectionFactory는 RedisConnectionFactory의 구현체로서 실질적인 역할 수행
    @Bean
    @Qualifier("2")
    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory(host, port); // 이렇게되면 0번 DB로 들어감
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
//        configuration.setPassword("1234");
        return new LettuceConnectionFactory(configuration);
    }

    // RedisTemplate은 redis와 상호작용할 때 redis key, value의 형식을 지정한다.
    @Bean
    @Qualifier("2")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("2") RedisConnectionFactory redisConnectionFactory) {
        // Object에는 주로 json이 들어옴
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // String을 Serialize하기 위한 툴
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // Json을 Serialize하기 위한 툴
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }



    // redisTemplate.opsForValue().set(key, value);
    // redisTemplate.opsForValue().get(key)
    // redisTemplate.opsForValue().increment 또는 decrement


    //== 3번 방 ==//
    @Bean
    @Qualifier("3")
    public RedisConnectionFactory redisStockConnectionFactory() {
//        return new LettuceConnectionFactory(host, port); // 이렇게되면 0번 DB로 들어감
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2);
//        configuration.setPassword("1234");
        return new LettuceConnectionFactory(configuration);
    }

    // RedisTemplate은 redis와 상호작용할 때 redis key, value의 형식을 지정한다.
    @Bean
    @Qualifier("3")
    public RedisTemplate<String, String> redisStockTemplate(@Qualifier("3") RedisConnectionFactory redisConnectionFactory) {
        // Object에는 주로 json이 들어옴
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // String을 Serialize하기 위한 툴
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // Json을 Serialize하기 위한 툴
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setEnableTransactionSupport(true); // 트랜잭션
        return redisTemplate;
    }


    //== 4번 방 ==//
    @Bean
    @Qualifier("4")
    public RedisConnectionFactory sseRedisFactory() {
//        return new LettuceConnectionFactory(host, port); // 이렇게되면 0번 DB로 들어감
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(3);
        return new LettuceConnectionFactory(configuration);
    }

    // RedisTemplate은 redis와 상호작용할 때 redis key, value의 형식을 지정한다.
    @Bean
    @Qualifier("4")
    public RedisTemplate<String, Object> sseTemplate(@Qualifier("4") RedisConnectionFactory sseFactory) {
        // Object에는 주로 json이 들어옴
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());


        //== 객체 안의 객체 직렬화  이슈로 인해 아래와 같이 serializer 커스텀
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        serializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(serializer);


        redisTemplate.setConnectionFactory(sseFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("4")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("4") RedisConnectionFactory sseFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(sseFactory);
        return container;
    }

    // 중복되는 내용의 코드. service 레이어에 있음
//    @Bean
//    public MessageListenerAdapter listenerAdapter(SseController sseController) {
//        // 레디스에 메시지가 발행되면 리슨하게 되고, 아래 코드를 통해 특정 메서드를 실행하도록 설정
//        return new MessageListenerAdapter(sseController, "onMessage");
//
//    }
}

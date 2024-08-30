//package org.beyond.ordersystem.common.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Slf4j
//@Configuration
//public class RabbitMqConfig {
//    // 큐 이름을 지정
//    public static final String STOCK_DECREASE_QUEUE = "stockDecreaseQueue";
//
//    @Value("${spring.rabbitmq.host}")
//    private String host;
//
//    @Value("${spring.rabbitmq.port}")
//    private int port;
//
//    @Value("${spring.rabbitmq.username}")
//    private String username;
//
//    @Value("${spring.rabbitmq.password}")
//    private String password;
//
//    @Value("${spring.rabbitmq.virtual-host}")
//    private String virtualHost;
//
//    @Bean
//    public /*org.springframework.amqp.core*/ Queue stockDecreaseQueue() {
//        return new Queue(STOCK_DECREASE_QUEUE, true);
//    }
//
//    @Bean
//    public /*springframework.amqp.rabbit.connection*/ConnectionFactory connectionFactory() {
//        CachingConnectionFactory factory = new CachingConnectionFactory();
//
//        factory.setHost(host);
//        factory.setPort(port);
//        factory.setUsername(username);
//        factory.setPassword(password);
//        factory.setVirtualHost(virtualHost);
//
//        log.info("line 46: {}, {}", username, password);
//
//        return factory;
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//
//        return rabbitTemplate;
//    }
//}

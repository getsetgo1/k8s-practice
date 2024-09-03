package org.beyond.ordersystem.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    // 내부 통신: 유레카에 등록된 .. 못적음
    @LoadBalanced
    public RestTemplate restTemplste() {
        return new RestTemplate();
    }
}

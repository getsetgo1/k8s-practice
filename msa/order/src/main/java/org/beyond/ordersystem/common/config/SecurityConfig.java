package org.beyond.ordersystem.common.config;

import lombok.RequiredArgsConstructor;
import org.beyond.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity // spring security 설정을 customizing하기 위함
@EnableGlobalMethodSecurity(prePostEnabled = true) // pre: 사전 검증, Post: 사후 검증. 사전 검증이 당연함
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // SecurityFilterChain과 httpSecurity는 상속관계 or 구현 관계일것임
        return httpSecurity
                .csrf().disable()
                .cors().and()
                .httpBasic().disable()
                .formLogin().disable()
                .authorizeRequests()
                // 아래 antMatchers는 인증 제외
                    .antMatchers("/member/create", "/", "/doLogin", "/refresh-token", "/product/list", "/api/presigned/**", "/member/reset-password")
                    .permitAll()
                .anyRequest().authenticated().and()
                // addFilterBefore: 사용자로부터 받아온 토큰이 정상인지 아닌지를 검증하는 코드.
                // 로그인시 사용자는 서버로부터 토큰을 발급받고, 매요청마다 해당 토큰을 http header 넣어 요청
                // 만약 세션 로그인이 아니라, 토큰 로그인일 경우 (세션이 stateful한 이유: 서버가 유저에 대한 값을 가지고 있기 때문이다.)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // jwtAuthFilter에서 토큰 검증하겠다는 뜻. 유저 네임과 패스워드를 비교해서 검증하겠다~라는 뜻
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
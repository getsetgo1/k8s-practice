package org.beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.security.sasl.AuthenticationException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            String bearerToken = ((HttpServletRequest) request).getHeader("Authorization");

            if(bearerToken != null && bearerToken.startsWith("Bearer")) {
                // 토큰은 관례적으로 Bearer로 시작하는 문구를 넣어서 요청한다.
                if(!bearerToken.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }

                String token = bearerToken.replace("Bearer ", "");
                // [token 검증] && [claims(=사용자 정보) 추출]
                // token 생성시에 사용한 secret 키 값을 넣어 토큰 검증에 사용한다.
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey)
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody();

                // Authentication 객체 생성(Authentication 객체를 만들기 위해서는 UserDetails 객체도 필요하다)

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);


            } else { // TODO: 삭제해야할 수도 있다.
//                throw new AuthenticationException("token이 없습니다."); // 그럼 여기서 생기는 예외는 안잡히나??
                chain.doFilter(request, response);
                return;
            }
            // filter chain에서 그 다음 filtering으로 넘어가도록 하는 메서드이다.
//            super.doFilter(request, response, chain)
            chain.doFilter(request, response);

        } catch(Exception e) { // 검증하다 무슨 에러가 발생하든지 401 던져버리기
//            e.printStackTrace();
//            log.error(e.getMessage());
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token not found");
        }
    }
}

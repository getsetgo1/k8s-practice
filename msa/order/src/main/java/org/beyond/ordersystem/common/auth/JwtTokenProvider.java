package org.beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    //== Access Token ==//
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;

    //== Refresh Token==//
    @Value("${jwt.secretKey-rt}")
    private String secretKeyRt;

    @Value("${jwt.expiration-rt}")
    private int expirationRt;

    public String createToken(String email, String role) {
        // claims는 사용자 정보이자 페이로드 정보이다.

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return token;
    }

    public String createRefreshToken(String email, String role) {
        // claims는 사용자 정보이자 페이로드 정보이다.
        Date now = new Date();
        Long two = now.getTime() + 14 * 24 * 60 * 60 * 1000L;

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return token;
    }

    public String getSecretKey() {
        return secretKey;
    }
}

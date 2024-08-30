package org.beyond.ordersystem.member.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.beyond.ordersystem.common.auth.JwtTokenProvider;
import org.beyond.ordersystem.common.dto.ErrorResponse;
import org.beyond.ordersystem.common.dto.SuccessResponse;
import org.beyond.ordersystem.member.domain.Member;
import org.beyond.ordersystem.member.dto.*;
import org.beyond.ordersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("2")
    private final RedisTemplate<String, String> redisTemplate;

    //== admin만 회원 목록 전체 조회 가능하다. ==//
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<SuccessResponse> memberList(@PageableDefault(size = 10) Pageable pageable) {
        Page<MemberResponse> memberList = memberService.memberList(pageable);
        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("멤버 리스트입니다.")
                .httpStatus(HttpStatus.OK)
                .result(memberList)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //== 본인은 본인 회원 정보만 조회 가능하다 ==//
    @GetMapping("/member/myinfo")
    public ResponseEntity<SuccessResponse> getMyInfo() {
        MemberResponse myInfo = memberService.getMyInfo();
        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("내 정보")
                .httpStatus(HttpStatus.OK)
                .result(myInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/member/create")
    public ResponseEntity<SuccessResponse> createMember(@Valid @RequestBody CreateMemberRequest createMemberRequest) {
        CreateMemberResponse member = memberService.createMember(createMemberRequest);
        SuccessResponse response = SuccessResponse.builder()
                .statusMessage("멤버 생성 완료.")
                .httpStatus(HttpStatus.CREATED)
                .result(member)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<SuccessResponse> doLogin(@RequestBody MemberLoginDto dto) {
        // email, password 일치하는지 검증
        Member member = memberService.login(dto);

        // 일치할 경우 accesssToken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, Duration.ofHours(240));
        // 생성된 토큰을 CommonResDto에 담아 사용자에게 리턴

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        SuccessResponse successResponse = new SuccessResponse(HttpStatus.OK, "login is successful", loginInfo);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {
        String refreshToken = dto.getRefreshToken();
        Claims claims;
        try {
            // 코드를 통해서 rt 검증
             claims = Jwts.parser()
                    .setSigningKey(jwtTokenProvider.getSecretKey())
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch(Exception e) {
            return new ResponseEntity<>(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "invalid refresh token"),
                    HttpStatus.BAD_REQUEST);
        }

        String email = claims.getSubject();
        String role = claims.get("role").toString();


        // redis를 조회하여 rt 추가 검증
        String storedRedisRt = redisTemplate.opsForValue().get(email);

//        log.info("line 120: {}", storedRedisRt.equals(refreshToken));

        if(storedRedisRt == null || !storedRedisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 리프레시 토큰");
        }



        String newAccessToken = jwtTokenProvider.createToken(email, role);

        Map<String, Object> info = new HashMap<>();
        info.put("accessToken", newAccessToken);
        SuccessResponse successResponse = new SuccessResponse(HttpStatus.OK, "access token is renewed", info);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PatchMapping("/member/reset-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        memberService.updatePassword(updatePasswordRequest);
        SuccessResponse successResponse = new SuccessResponse(HttpStatus.OK, "비밀번호 변경", null);

        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
package org.beyond.ordersystem.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRefreshDto {
    private String refreshToken;
}

package org.beyond.ordersystem.member.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String email;
    private String asIsPassword;
    private String toBePassword;
}

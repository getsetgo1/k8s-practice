package org.beyond.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
public class SuccessResponse extends CustomResponse {
    String statusMessage;
    Object result;

    @Builder
    public SuccessResponse(HttpStatus httpStatus, String statusMessage, Object result) {
        super(httpStatus.value());
        this.statusMessage = httpStatus.getReasonPhrase();
        this.result = result;
    }
}

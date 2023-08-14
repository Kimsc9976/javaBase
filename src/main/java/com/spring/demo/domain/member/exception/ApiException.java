package com.spring.demo.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode error;

    public ApiException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public ErrorCode getCode() {
        return error;
    }

}

package com.amerd.schoolbook.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ErrorResponse {

    public static final String RESOURCE_NOT_FOUND = "The requested resource was not found";

    private LocalDateTime timeStamp;
    private int status;
    private String reason;
    private String message;

    public static ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus httpStatus, String message) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .message(message)
                .status(httpStatus.value())
                .reason(httpStatus.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}

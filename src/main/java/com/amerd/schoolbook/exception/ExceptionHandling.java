package com.amerd.schoolbook.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponse> userExistsException(UserExistsException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> internalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus httpStatus, String message) {
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

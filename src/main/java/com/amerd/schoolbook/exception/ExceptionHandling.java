package com.amerd.schoolbook.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("", ex);
        List<String> messages = ex.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return createErrorResponse(HttpStatus.BAD_REQUEST, String.valueOf(messages));
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
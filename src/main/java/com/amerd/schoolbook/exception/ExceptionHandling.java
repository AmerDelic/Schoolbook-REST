package com.amerd.schoolbook.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.amerd.schoolbook.exception.ErrorResponse.createErrorResponse;

@Slf4j
@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.I_AM_A_TEAPOT, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> constraintViolationException(ConstraintViolationException ex) {
        log.error("", ex);
        StringBuilder stringBuilder = new StringBuilder();
        ex.getConstraintViolations().forEach(constraintViolation -> {
            stringBuilder.append(constraintViolation.getMessage());
            stringBuilder.append(".\n");
        });
        return createErrorResponse(HttpStatus.BAD_REQUEST, stringBuilder.toString());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> tokenExpiredException(TokenExpiredException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<ErrorResponse> jWTVerificationException(JWTVerificationException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(NoSuchMethodError.class)
    public ResponseEntity<ErrorResponse> noSuchMethodError(NoSuchMethodError ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(EntityNotFoundException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> lockedException(LockedException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ErrorResponse> executionException(ExecutionException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentialsException(BadCredentialsException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<ErrorResponse> jWTDecodeException(JWTDecodeException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validationException(ValidationException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("", ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Parse error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("", ex);
        List<String> messages = ex.getAllErrors().stream().map(er -> Optional.ofNullable(er.getDefaultMessage()).orElse("")).toList();
        return createErrorResponse(HttpStatus.BAD_REQUEST, String.valueOf(messages));
    }

}

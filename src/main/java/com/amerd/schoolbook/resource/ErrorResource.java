package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.amerd.schoolbook.common.constant.Endpoint.ERROR_PATH;
import static com.amerd.schoolbook.exception.ErrorResponse.createErrorResponse;

@Slf4j
@RestController
public class ErrorResource implements ErrorController {

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<ErrorResponse> handleErrorResponse(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        log.error("", exception);
        return createErrorResponse(HttpStatus.valueOf(statusCode), exception.getMessage());
    }
}

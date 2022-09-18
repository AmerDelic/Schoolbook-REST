package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.exception.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.amerd.schoolbook.common.constant.Endpoint.ERROR_PATH;
import static com.amerd.schoolbook.exception.ErrorResponse.RESOURCE_NOT_FOUND;
import static com.amerd.schoolbook.exception.ErrorResponse.createErrorResponse;

@RestController
public class ErrorResource implements ErrorController {

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<ErrorResponse> notFound404() {
        return createErrorResponse(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND);
    }
}

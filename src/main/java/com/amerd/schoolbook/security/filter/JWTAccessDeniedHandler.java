package com.amerd.schoolbook.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static com.amerd.schoolbook.common.constant.SecurityConstant.ACCESS_DENIED_MESSAGE;

@Component
public class JWTAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ResponseEntity<String> responseEntity =
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESS_DENIED_MESSAGE);
        try (OutputStream outputStream = response.getOutputStream()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, responseEntity);
            outputStream.flush();
        }
    }
}

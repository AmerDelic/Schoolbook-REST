package com.amerd.schoolbook.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static com.amerd.schoolbook.common.constant.SecurityConstant.FORBIDDEN_MESSAGE;

@Component
public class JWTAuthEntryPoint extends Http403ForbiddenEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException {

        ResponseEntity<String> responseEntity =
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        try (OutputStream outputStream = response.getOutputStream()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, responseEntity);
            outputStream.flush();
        }
    }
}

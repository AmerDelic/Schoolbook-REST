package com.amerd.schoolbook.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomHttpResponse<T> {
    private LocalDateTime timestamp;
    private T resource;
    private String message;

    public CustomHttpResponse(T resource, String message) {
        this.resource = resource;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

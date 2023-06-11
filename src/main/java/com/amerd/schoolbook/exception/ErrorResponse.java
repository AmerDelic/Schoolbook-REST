package com.amerd.schoolbook.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
public class ErrorResponse {

    public static final String RESOURCE_NOT_FOUND = "The requested resource was not found";
    private LocalDateTime timeStamp;
    private int status;
    private String reason;
    private String message;
}

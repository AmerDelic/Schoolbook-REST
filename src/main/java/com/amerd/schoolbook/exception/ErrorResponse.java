package com.amerd.schoolbook.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timeStamp;
    private int status;
    private String reason;
    private String message;
}

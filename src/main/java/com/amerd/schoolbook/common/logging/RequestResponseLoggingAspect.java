package com.amerd.schoolbook.common.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Aspect
@Component
public class RequestResponseLoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.amerd.schoolbook.resource..*) && within(@org.springframework.web.bind.annotation.RestController *)")
    public void loggableMethods() {
    }

    @Before("loggableMethods()")
    public void logRequest(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // Log request details
        logger.info("Request URL: {}", request.getRequestURL());
        logger.info("HTTP Method: {}", request.getMethod());
        logger.info("Headers:");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            logger.info("{}: {}", headerName, headerValue);
        }

        logger.info("Payload: {}", joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "loggableMethods()", returning = "responseEntity")
    public void logResponse(JoinPoint joinPoint, ResponseEntity<?> responseEntity) {
        logger.info("Response: {}", responseEntity);
    }
}

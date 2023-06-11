package com.amerd.schoolbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SchoolbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolbookApplication.class, args);
    }

}

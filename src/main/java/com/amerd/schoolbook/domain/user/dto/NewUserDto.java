package com.amerd.schoolbook.domain.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class NewUserDto {

    @NotNull(message = "Username is required")
    private String username;
    @NotNull(message = "Password is required")
    private String password;
    @Email
    @NotNull(message = "Valid email is required")
    private String email;
    private String firstName;
    private String lastName;
}

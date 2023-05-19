package com.amerd.schoolbook.domain.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserRegistrationDto {
    @NotNull(message = "Username is required")
    private String username;
    private String password;
    @Email(message = "Must be a well formed email")
    @NotNull(message = "Valid email is required")
    private String email;
    private String firstName;
    private String lastName;
}

package com.amerd.schoolbook.domain.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserRegistrationDto {
    @NotNull(message = "Username is required")
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @Email(message = "Must be a well formed email")
    @NotNull(message = "Valid email is required")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    private String firstName;
    private String lastName;
}

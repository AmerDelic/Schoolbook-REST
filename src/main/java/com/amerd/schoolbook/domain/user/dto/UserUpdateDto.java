package com.amerd.schoolbook.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    @Email(message = "Must be a well formed email")
    private String email;
    private String role;
    private MultipartFile profileImage;
    private boolean isEnabled;
    private boolean isNonLocked;
}

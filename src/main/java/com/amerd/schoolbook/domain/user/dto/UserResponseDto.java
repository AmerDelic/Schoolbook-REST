package com.amerd.schoolbook.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

    private Long id;
    private String username;
    private String publicId;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;

    private String role;
    private String[] authorities;

    private boolean isEnabled;
    private boolean isNonLocked;
}

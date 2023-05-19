package com.amerd.schoolbook.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldNameConstants
public class UserResponseDto {

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

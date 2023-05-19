package com.amerd.schoolbook.domain.user;

import com.amerd.schoolbook.domain.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "email"}),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
@NoArgsConstructor
@FieldNameConstants
public class User extends BaseEntity {

    @NotBlank(message = "Username must not be null or blank")
    private String username;
    @NotBlank(message = "Password must not be null or blank")
    private String password;
    private String publicId;
    private String firstName;
    private String lastName;
    @NotBlank(message = "Email must not be null or blank")
    @Email(message = "Email must a well formed email address")
    @Column(nullable = false)
    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    private String role;
    @Column(columnDefinition = "boolean default true")
    private boolean isEnabled;
    @Column(columnDefinition = "boolean default false")
    private boolean isNonLocked;
}

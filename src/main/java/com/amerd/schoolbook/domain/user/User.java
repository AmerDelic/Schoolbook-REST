package com.amerd.schoolbook.domain.user;

import com.amerd.schoolbook.domain.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})})
@NoArgsConstructor
@FieldNameConstants
public class User extends BaseEntity {

    @NotNull(message = "Username must not be null")
    private String username;
    @NotNull(message = "Password must not be null")
    private String password;
    private String publicId;
    private String firstName;
    private String lastName;
    @NotNull(message = "Email must not be null")
    @Email
    @Column(nullable = false)
    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    private String role;
    @Transient
    private String[] authorities;
    @Column(columnDefinition = "boolean default true")
    private boolean isEnabled;
    @Column(columnDefinition = "boolean default false")
    private boolean isNonLocked;
}

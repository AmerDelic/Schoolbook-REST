package com.amerd.schoolbook.domain.user;

import com.amerd.schoolbook.domain.base.BaseEntity;
import com.amerd.schoolbook.domain.user.permission.Authority;
import com.amerd.schoolbook.domain.user.permission.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})})
public class User extends BaseEntity {

    @NotNull
    private String username;
    @NotNull
    private String password;
    private String publicId;
    private String firstName;
    private String lastName;
    @NonNull
    @Email
    @Column(nullable = false)
    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    @ManyToMany(targetEntity = Role.class)
    private Set<Role> roles;
    @ManyToMany(targetEntity = Authority.class)
    private Set<Authority> authorities;
    @Column(columnDefinition = "boolean default true")
    private boolean isEnabled;
    @Column(columnDefinition = "boolean default false")
    private boolean isNonLocked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (this.isEnabled() != user.isEnabled()) return false;
        if (this.isNonLocked() != user.isNonLocked()) return false;
        if (!getUsername().equals(user.getUsername())) return false;
        if (!getPassword().equals(user.getPassword())) return false;
        if (getPublicId() != null ? !getPublicId().equals(user.getPublicId()) : user.getPublicId() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(user.getFirstName()) : user.getFirstName() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(user.getLastName()) : user.getLastName() != null)
            return false;
        if (!getEmail().equals(user.getEmail())) return false;
        if (getProfileImageUrl() != null ? !getProfileImageUrl().equals(user.getProfileImageUrl()) : user.getProfileImageUrl() != null)
            return false;
        if (getLastLoginDate() != null ? !getLastLoginDate().equals(user.getLastLoginDate()) : user.getLastLoginDate() != null)
            return false;
        return getLastLoginDateDisplay() != null ? getLastLoginDateDisplay().equals(user.getLastLoginDateDisplay()) : user.getLastLoginDateDisplay() == null;
    }

    @Override
    public int hashCode() {
        int result = getUsername().hashCode();
        result = 31 * result + getPassword().hashCode();
        result = 31 * result + (getPublicId() != null ? getPublicId().hashCode() : 0);
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + getEmail().hashCode();
        result = 31 * result + (getProfileImageUrl() != null ? getProfileImageUrl().hashCode() : 0);
        result = 31 * result + (getLastLoginDate() != null ? getLastLoginDate().hashCode() : 0);
        result = 31 * result + (getLastLoginDateDisplay() != null ? getLastLoginDateDisplay().hashCode() : 0);
        result = 31 * result + (this.isEnabled() ? 1 : 0);
        result = 31 * result + (this.isNonLocked() ? 1 : 0);
        return result;
    }
}

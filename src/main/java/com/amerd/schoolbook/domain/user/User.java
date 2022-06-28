package com.amerd.schoolbook.domain.user;

import com.amerd.schoolbook.domain.base.BaseEntity;
import com.amerd.schoolbook.domain.user.permission.Authority;
import com.amerd.schoolbook.domain.user.permission.Role;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})})
public class User extends BaseEntity {

    private String username;
    private String password;
    private String publicId;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    @ManyToMany(targetEntity = Role.class)
    private Set<Role> roles;
    @ManyToMany(targetEntity = Authority.class)
    private Set<Authority> authorities;

    private boolean isActive;
    private boolean isLocked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (isActive() != user.isActive()) return false;
        if (isLocked() != user.isLocked()) return false;
        if (getUsername() != null ? !getUsername().equals(user.getUsername()) : user.getUsername() != null)
            return false;
        if (getPassword() != null ? !getPassword().equals(user.getPassword()) : user.getPassword() != null)
            return false;
        if (getPublicId() != null ? !getPublicId().equals(user.getPublicId()) : user.getPublicId() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(user.getFirstName()) : user.getFirstName() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(user.getLastName()) : user.getLastName() != null)
            return false;
        if (getEmail() != null ? !getEmail().equals(user.getEmail()) : user.getEmail() != null) return false;
        if (getProfileImageUrl() != null ? !getProfileImageUrl().equals(user.getProfileImageUrl()) : user.getProfileImageUrl() != null)
            return false;
        if (getLastLoginDate() != null ? !getLastLoginDate().equals(user.getLastLoginDate()) : user.getLastLoginDate() != null)
            return false;
        return getLastLoginDateDisplay() != null ? getLastLoginDateDisplay().equals(user.getLastLoginDateDisplay()) : user.getLastLoginDateDisplay() == null;
    }

    @Override
    public int hashCode() {
        int result = getUsername() != null ? getUsername().hashCode() : 0;
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getPublicId() != null ? getPublicId().hashCode() : 0);
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        result = 31 * result + (getProfileImageUrl() != null ? getProfileImageUrl().hashCode() : 0);
        result = 31 * result + (getLastLoginDate() != null ? getLastLoginDate().hashCode() : 0);
        result = 31 * result + (getLastLoginDateDisplay() != null ? getLastLoginDateDisplay().hashCode() : 0);
        result = 31 * result + (isActive() ? 1 : 0);
        result = 31 * result + (isLocked() ? 1 : 0);
        return result;
    }
}

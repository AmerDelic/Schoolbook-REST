package com.amerd.schoolbook.repo;

import com.amerd.schoolbook.domain.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @NotNull Optional<User> findById(@NotNull Long id);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);
}

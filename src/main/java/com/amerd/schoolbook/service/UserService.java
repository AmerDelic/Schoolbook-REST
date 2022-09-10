package com.amerd.schoolbook.service;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    User findByUsernameOrThrow(String username);

    User findByEmailOrThrow(String email);

    User register(String firstName, String lastName, String username, String password, String email);

    List<User> getAllUsers();

    CustomPage<User> getAllUsersPaged(Pageable pageable);
}

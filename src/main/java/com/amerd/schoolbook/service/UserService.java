package com.amerd.schoolbook.service;

import com.amerd.schoolbook.domain.user.User;

import java.util.List;

public interface UserService {

    User findByUsernameOrThrow(String username);

    User findByEmailOrThrow(String email);

    User register(String firstName, String lastName, String username, String password, String email);

    List<User> getAllUsers();

}

package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserService {

    User findByIdOrThrow(Long id);

    User findByUsernameOrThrow(String username);

    User findByEmailOrThrow(String email);

    User register(String firstName, String lastName, String username, String password, String email);

    List<User> getAllUsers();

    CustomPage<User> getAllUsersPaged(Pageable pageable);

    User update(Long id, JsonNode userUpdate) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}

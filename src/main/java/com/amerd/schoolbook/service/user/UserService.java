package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserService {

    User create(JsonNode request) throws IOException;

    User findByIdOrThrow(Long id);

    User findByUsernameOrThrow(String username);

    User findByEmailOrThrow(String email);

    User register(JsonNode request) throws IOException;

    List<User> getAllUsers();

    CustomPage<User> getAllUsersPaged(Pageable pageable);

    User update(String username, JsonNode userUpdate) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException;

    String resetPassword(@Email String email);

    String delete(Long id);

    User updateProfileImage(String username, MultipartFile profileImage) throws IOException;

    byte[] getProfileImage(String username) throws IOException;
}

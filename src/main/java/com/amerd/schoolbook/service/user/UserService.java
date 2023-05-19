package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserCreationDto;
import com.amerd.schoolbook.domain.user.dto.UserRegistrationDto;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.UserUpdateDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import java.io.IOException;
import java.util.List;

public interface UserService {

    User create(UserCreationDto dto) throws IOException;

    User save(User user);

    User findByIdOrThrow(Long id);

    User findByUsernameOrThrow(String username);

    User findByEmailOrThrow(String email);

    User register(UserRegistrationDto dto) throws IOException;

    CustomPage<UserResponseDto> getAllUsersPaged(Pageable pageable);

    User update(String username, UserUpdateDto dto) throws IOException;

    String resetPassword(@Email String email);

    String delete(Long id);

    User updateProfileImage(String username, MultipartFile profileImage) throws IOException;

    byte[] getProfileImage(String username) throws IOException;
}

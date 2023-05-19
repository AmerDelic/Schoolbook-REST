package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserCreationDto;
import com.amerd.schoolbook.domain.user.dto.UserRegistrationDto;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.UserUpdateDto;
import com.amerd.schoolbook.domain.user.dto.mapper.UserMapper;
import com.amerd.schoolbook.exception.UserNotFoundException;
import com.amerd.schoolbook.repo.UserRepository;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.login.LoginAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.ValidationException;
import javax.validation.constraints.Email;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.amerd.schoolbook.common.constant.FileConstant.DEFAULT_IMAGE_PATH;
import static com.amerd.schoolbook.common.constant.FileConstant.DEFAULT_PROFILE_IMAGE;
import static com.amerd.schoolbook.common.constant.FileConstant.DIRECTORY_CREATED;
import static com.amerd.schoolbook.common.constant.FileConstant.DOT;
import static com.amerd.schoolbook.common.constant.FileConstant.FILE_SAVED_IN_FILE_SYSTEM;
import static com.amerd.schoolbook.common.constant.FileConstant.JPG_EXTENSION;
import static com.amerd.schoolbook.common.constant.FileConstant.USER_FOLDER;
import static com.amerd.schoolbook.common.constant.FileConstant.USER_IMAGE_PATH;
import static com.amerd.schoolbook.common.constant.FileConstant.USER_TEMP_FOLDER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final ObjectMapper mapper;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsernameOrThrow(username);
        validateLoginAttempt(user);
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        user = userRepository.save(user);
        return new UserPrincipal(user);
    }

    private void validateLoginAttempt(User user) {
        if (user.isNonLocked()) {
            if (loginAttemptService.exceededMaxAttempts(user.getUsername())) {
                user.setNonLocked(false);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User of id=[%d] not found", id)));
    }

    @Override
    public User findByUsernameOrThrow(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User [%s] not found", username)));
    }

    @Override
    public User findByEmailOrThrow(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email [%s] not found", email)));
    }

    @Override
    public CustomPage<UserResponseDto> getAllUsersPaged(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        List<UserResponseDto> userResponseDtos = usersPage.stream().map(userMapper::toResponseDto).toList();
        return new CustomPage<>(
                userResponseDtos, pageable.getPageNumber(), usersPage.getTotalElements(), usersPage.getTotalPages());
    }

    @Override
    public User create(UserCreationDto dto) throws IOException {
        validateNewUserData(dto.getUsername(), dto.getEmail());
        User newUser = userMapper.creationDtoToEntity(dto);
        newUser.setPassword(encodePassword(newUser.getPassword()));
        profileImageUpdate(newUser, null);
        return save(newUser);
    }

    @Override
    public User register(UserRegistrationDto dto) throws IOException {
        validateNewUserData(dto.getUsername(), dto.getEmail());
        User newUser = userMapper.registrationDtoToEntity(dto);
        newUser.setPassword(encodePassword(newUser.getPassword()));
        profileImageUpdate(newUser, null);
        return save(newUser);
    }

    @Override
    public User update(String username, UserUpdateDto dto) {
        User updatedUser = userMapper.updateEntity(dto, findByUsernameOrThrow(username));
        if (StringUtils.hasText(dto.getPassword())) {
            updatedUser.setPassword(encodePassword(updatedUser.getPassword()));
        }
        return save(updatedUser);
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws IOException {
        User user = findByUsernameOrThrow(username);
        return save(profileImageUpdate(user, profileImage));
    }

    @Override
    public String resetPassword(@Email String email) {
        User user = findByEmailOrThrow(email);
        String newPassword = userMapper.generatePassword();
        user.setPassword(encodePassword(newPassword));
        save(user);
        return newPassword;
    }

    @Override
    public String delete(Long id) {
        User user = findByIdOrThrow(id);
        userRepository.delete(user);
        return String.format("Deleted user: %s [id=%s]", user.getUsername(), user.getId());
    }

    public User save(User user) {
        return getUserRepository().save(user);
    }

    // TODO: separate image logic into own service so it can be injected independently
    @Override
    public byte[] getProfileImage(String username) throws IOException {
        Path profileImageFolder = Paths.get(URI.create(USER_FOLDER + username.toLowerCase())).toAbsolutePath().normalize();
        Path imagePath = profileImageFolder.resolve(username.toLowerCase() + DOT + JPG_EXTENSION);
        if (Files.exists(imagePath)) {
            return Files.readAllBytes(imagePath);
        }
        createTempFolderAndImageIfNotExist();
        Path userTempFolder = Paths.get(URI.create(USER_TEMP_FOLDER)).toAbsolutePath().normalize();
        Path tempImagePath = userTempFolder.resolve(DEFAULT_PROFILE_IMAGE + DOT + JPG_EXTENSION);
        if (Files.exists(tempImagePath)) {
            return Files.readAllBytes(tempImagePath);
        }
        return new byte[0];
    }

    private User profileImageUpdate(User user, MultipartFile profileImage) throws IOException {
        String username = user.getUsername().toLowerCase();
        if (profileImage != null && profileImage.getBytes().length > 0) {
            Path userFolder = Paths.get(URI.create(USER_FOLDER + username)).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                log.info(String.format(DIRECTORY_CREATED, username, userFolder));
            }
            Files.deleteIfExists(Paths.get(userFolder + username.toLowerCase() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(
                    username + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(resolveProfileImgUrl(username));
            log.info(String.format(FILE_SAVED_IN_FILE_SYSTEM, profileImage.getOriginalFilename()));
        } else {
            createTempFolderAndImageIfNotExist();
            user.setProfileImageUrl(resolveProfileImgUrl(username));
            log.info("Profile image file is null -> using default image");
        }
        return user;
    }

    private void createTempFolderAndImageIfNotExist() throws IOException {
        Path userTempFolder = Paths.get(URI.create(USER_TEMP_FOLDER)).toAbsolutePath().normalize();
        if (!Files.exists(userTempFolder)) {
            Files.createDirectories(userTempFolder);
            log.info(String.format(DIRECTORY_CREATED, "temporary images", userTempFolder));
            Files.copy(Path.of(DEFAULT_IMAGE_PATH), userTempFolder.resolve(
                    DEFAULT_PROFILE_IMAGE + DOT + JPG_EXTENSION));
        }
    }

    private String resolveProfileImgUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(String.format(USER_IMAGE_PATH, username.toLowerCase())).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void validateNewUserData(String username, String email) {
        Optional<User> user = getUserRepository().findUserByUsername(username);
        if (user.isPresent()) throw new ValidationException("Username already in use");
        user = getUserRepository().findUserByEmail(email);
        if (user.isPresent()) throw new ValidationException("Email already in use");

    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
        //TODO:
    }
}

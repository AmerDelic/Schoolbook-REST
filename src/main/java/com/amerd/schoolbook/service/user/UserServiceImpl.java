package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.exception.UserNotFoundException;
import com.amerd.schoolbook.repo.UserRepository;
import com.amerd.schoolbook.security.Role;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.login.LoginAttemptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import javax.validation.constraints.Email;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
import static com.amerd.schoolbook.common.jsonutil.JsonUtil.getNodeStringValueOrNull;
import static com.amerd.schoolbook.common.jsonutil.JsonUtil.getOptionalNodeStringValue;
import static com.amerd.schoolbook.service.user.UserOperation.CREATE;
import static com.amerd.schoolbook.service.user.UserOperation.REGISTER;
import static com.amerd.schoolbook.service.user.UserOperation.UPDATE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Validated
@Service
@Transactional
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    private final ObjectMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsernameOrThrow(username);
        validateLoginAttempt(user);
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        user = userRepository.save(user);
        log.info("Found user [{}]", username);
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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public CustomPage<User> getAllUsersPaged(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return new CustomPage<>(
                usersPage.getContent(), pageable.getPageNumber(), usersPage.getTotalElements(), usersPage.getTotalPages());
    }

    @Override
    public User create(JsonNode request) throws IOException {
        User newUser = doUserOperation(new User(), request, CREATE);
        profileImageUpdate(newUser, null);
        return save(newUser);
    }

    @Override
    public User register(JsonNode request) throws IOException {
        User newUser = doUserOperation(new User(), request, REGISTER);
        profileImageUpdate(newUser, null);
        return save(newUser);
    }

    @Override
    public User update(String username, JsonNode request) {
        User oldState = findByUsernameOrThrow(username);
        User newState = doUserOperation(oldState, request, UserOperation.UPDATE);
        return save(newState);
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws IOException {
        User user = findByUsernameOrThrow(username);
        return save(profileImageUpdate(user, profileImage));
    }

    @Override
    public String resetPassword(@Email String email) {
        User user = findByEmailOrThrow(email);
        String newPassword = generatePassword();
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

    private User save(User user) {
        return getUserRepository().save(user);
    }

    private User doUserOperation(User user, JsonNode request, String operation) {
        String newUsername, newPassword, newEmail, newFirstName, newLastName, newRole, newIsEnabled, newIsNonLocked;
        validateNewUserData(request);

        newUsername = getNodeStringValueOrNull(request, User.Fields.username);
        newPassword = getNodeStringValueOrNull(request, User.Fields.password);
        newEmail = getNodeStringValueOrNull(request, User.Fields.email);
        newFirstName = getNodeStringValueOrNull(request, User.Fields.firstName);
        newLastName = getNodeStringValueOrNull(request, User.Fields.lastName);

        if (StringUtils.hasText(newUsername)) user.setUsername(newUsername);
        if (StringUtils.hasText(newPassword)) {
            user.setPassword(encodePassword(newPassword));
        } else if (operation.equalsIgnoreCase(CREATE) || operation.equalsIgnoreCase(REGISTER)) {
            user.setPassword(encodePassword(generatePassword()));
        }
        if (StringUtils.hasText(newEmail)) user.setEmail(newEmail);
        if (StringUtils.hasText(newFirstName)) user.setFirstName(newFirstName);
        if (StringUtils.hasText(newLastName)) user.setLastName(newLastName);

        if (operation.equalsIgnoreCase(UPDATE)) return user;
        if (operation.equalsIgnoreCase(CREATE)) {
            newRole = getOptionalNodeStringValue(request, User.Fields.role).orElse(Role.ROLE_USER.name());
            newIsEnabled = getOptionalNodeStringValue(request, User.Fields.isEnabled).orElse("false");
            newIsNonLocked = getOptionalNodeStringValue(request, User.Fields.isNonLocked).orElse("false");
            user.setRole(getRoleEnumName(newRole));
            user.setAuthorities(Role.valueOf(newRole).getAuthorities());
            user.setEnabled(Boolean.parseBoolean(newIsEnabled));
            user.setNonLocked(Boolean.parseBoolean(newIsNonLocked));
            user.setPublicId(generatePublicId());
        }
        if (operation.equalsIgnoreCase(REGISTER)) {
            user.setRole(Role.ROLE_USER.name());
            user.setAuthorities(Role.ROLE_USER.getAuthorities());
            user.setEnabled(true);
            user.setNonLocked(true);
            user.setPublicId(generatePublicId());
        }
        return user;
    }

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
            return user;
        } else {
            createTempFolderAndImageIfNotExist();
            user.setProfileImageUrl(resolveProfileImgUrl(username));
            log.info("Profile image file is null -> using default image");
            return user;
        }
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

    private String getRoleEnumName(String role) {
        return Role.valueOf(role).name();
    }

    private String resolveProfileImgUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(String.format(USER_IMAGE_PATH, username.toLowerCase())).toUriString();
    }

    @NotNull
    private static String generatePassword() {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(10);
    }

    @NotNull
    private static String generatePublicId() {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(8).toUpperCase(Locale.ROOT);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void validateNewUserData(JsonNode update) {
        if (update.hasNonNull(User.Fields.username)) {
            String newUsername = update.get(User.Fields.username).asText();
            if (newUsername.isBlank()) throw new ValidationException("Username cannot be blank");
            Optional<User> user = getUserRepository().findUserByUsername(newUsername);
            if (user.isPresent()) throw new ValidationException("Username already in use");
        }
        if (update.hasNonNull(User.Fields.email)) {
            String newEmail = update.get(User.Fields.email).asText();
            if (newEmail.isBlank()) throw new ValidationException("Email cannot be blank");
            Optional<User> user = getUserRepository().findUserByEmail(newEmail);
            if (user.isPresent()) throw new ValidationException("Email already in use");
        }
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        //TODO: find a nice way to save this
    }
}

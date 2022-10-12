package com.amerd.schoolbook.service.user;

import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.exception.UserExistsException;
import com.amerd.schoolbook.exception.UserNotFoundException;
import com.amerd.schoolbook.repo.UserRepository;
import com.amerd.schoolbook.security.Role;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.login.LoginAttemptService;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import javax.validation.constraints.Email;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Validated
@Service
@Transactional
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    public static final String USER_IMAGE_PROFILE_TEMP = "/user/image/profile/temp";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

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
    public User addNewUser(String firstName, String lastName, String username, String email, String role,
                           boolean isNonLocked, boolean isEnabled, MultipartFile profileImage) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(isEnabled);
        user.setNonLocked(isNonLocked);
        //  user.setProfileImageUrl();
        return null;
    }

    @Override
    public User register(String firstName, String lastName, String username, String password, String email) {
        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(username);
        newUser.setPassword(encodePassword(password));
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setNonLocked(true);
        newUser.setRole(Role.ROLE_USER.name());
        newUser.setAuthorities(Role.ROLE_USER.getAuthorities());
        newUser.setProfileImageUrl(getTemporaryImgUrl());
        return save(validateNewUser(newUser));
    }

    @Override
    public User update(Long id, JsonNode userUpdate) {
        validateUserUpdate(userUpdate);
        User user = findByIdOrThrow(id);
        Field[] fieldsArray = User.class.getDeclaredFields();
        Method[] methodsArray = User.class.getDeclaredMethods();
        Predicate<String> updateHasField = userUpdate::hasNonNull;
        List<String> fields = Arrays.stream(fieldsArray)
                .map(Field::getName)
                .filter(updateHasField)
                .collect(Collectors.toList());
        Consumer<String> updateFunction = (field) -> Arrays.stream(methodsArray).forEach((method -> {
            if (method.getName().startsWith("set") && method.getName().substring(3).equalsIgnoreCase(field)) {
                try {
                    method.invoke(user, userUpdate.get(field).asText());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        fields.forEach(updateFunction);
        if (fields.contains(User.Fields.password)) user.setPassword(encodePassword(user.getPassword()));
        return save(user);
    }

    @Override
    public User updateProfileImage(Long id, MultipartFile profileImage) {
        return null;
    }

    @Override
    public String resetPassword(@Email String email) {
        User user = findByEmailOrThrow(email);
        String newPassword = generatePassword();
        user.setPassword(encodePassword(newPassword));
        save(user);
        return newPassword;
    }

    @NotNull
    private static String generatePassword() {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(10);
    }

    @Override
    public void delete(Long id) {
        User user = findByIdOrThrow(id);
        userRepository.delete(user);
    }

    private User save(User user) {
        return getUserRepository().save(user);
    }

    private String getTemporaryImgUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PROFILE_TEMP).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private User validateNewUser(User user) {
        if (userRepository.findUserByUsername(user.getUsername()).isPresent()) {
            throw new UserExistsException(String.format("Username '%s' already in use", user.getUsername()));
        }
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new UserExistsException(String.format("Email '%s' already in use", user.getEmail()));
        }
        return user;
    }

    private void validateUserUpdate(JsonNode update) {
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
}

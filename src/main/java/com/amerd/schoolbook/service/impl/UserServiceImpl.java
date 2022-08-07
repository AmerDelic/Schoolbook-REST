package com.amerd.schoolbook.service.impl;

import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.exception.UserExistsException;
import com.amerd.schoolbook.exception.UserNotFoundException;
import com.amerd.schoolbook.repo.UserRepository;
import com.amerd.schoolbook.security.Role;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsernameOrThrow(username);
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        user = userRepository.save(user);
        log.info("Found user [{}]", username);
        return new UserPrincipal(user);
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
        return save(validateUser(newUser));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
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

    private User validateUser(User user) {
        if (userRepository.findUserByUsername(user.getUsername()).isPresent()) {
            throw new UserExistsException(String.format("Username '%s' already in use", user.getUsername()));
        }
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new UserExistsException(String.format("Email '%s' already in use", user.getEmail()));
        }
        return user;
    }

}

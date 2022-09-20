package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.common.constant.Endpoint;
import com.amerd.schoolbook.common.constant.SecurityConstant;
import com.amerd.schoolbook.common.response.CustomHttpResponse;
import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.NewUserDto;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.mapper.UserMapper;
import com.amerd.schoolbook.exception.ExceptionHandling;
import com.amerd.schoolbook.security.provider.JWTProvider;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.ValidationException;

@RestController
@RequestMapping(Endpoint.USER)
@RequiredArgsConstructor
public class UserResource extends ExceptionHandling {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<CustomPage<User>>> getAll(Pageable pageable) {
        CustomPage<User> users = userService.getAllUsersPaged(pageable);
        return ResponseEntity.ok(new CustomHttpResponse<>(users, "All users"));
    }

    @PostMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> create(@Valid @RequestBody NewUserDto newUserDto) {
        User newUser = userService.register(
                newUserDto.getFirstName(), newUserDto.getLastName(), newUserDto.getUsername(), newUserDto.getPassword(),
                newUserDto.getEmail());
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(newUser), "Created user"));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> login(@RequestBody JsonNode login) {
        if (!login.hasNonNull(User.Fields.username)) throw new ValidationException("Username required");
        if (!login.hasNonNull(User.Fields.password)) throw new ValidationException("Password required");
        String username = login.get(User.Fields.username).asText();
        String password = login.get(User.Fields.password).asText();
        authenticate(username, password);
        User loggedUser = userService.findByUsernameOrThrow(username);
        UserPrincipal userPrincipal = new UserPrincipal(loggedUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(new CustomHttpResponse<>(
                userMapper.toResponseDto(loggedUser), "Logged in"), jwtHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}

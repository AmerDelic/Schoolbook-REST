package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.common.constant.Endpoint;
import com.amerd.schoolbook.common.constant.SecurityConstant;
import com.amerd.schoolbook.common.response.CustomHttpResponse;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.mapper.UserMapper;
import com.amerd.schoolbook.exception.ExceptionHandling;
import com.amerd.schoolbook.security.provider.JWTProvider;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Endpoint.USER)
@RequiredArgsConstructor
public class UserResource {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;

    @GetMapping()
    public ResponseEntity<CustomHttpResponse<List<User>>> getAll() {
        List<User> users = userService.getAllUsers();
        var response = new CustomHttpResponse<>(users, "All users");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/new")
    public ResponseEntity<CustomHttpResponse<User>> create(@RequestBody User user) {
        User user1 = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getPassword(), user.getEmail());
        return ResponseEntity.ok(new CustomHttpResponse<>(user1, "Created user"));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loggedUser = userService.findByUsernameOrThrow(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loggedUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loggedUser, jwtHeader, HttpStatus.OK);
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

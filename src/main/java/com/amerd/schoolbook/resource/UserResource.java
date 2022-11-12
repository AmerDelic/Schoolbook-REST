package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.common.constant.Endpoint;
import com.amerd.schoolbook.common.constant.SecurityConstant;
import com.amerd.schoolbook.common.response.CustomHttpResponse;
import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.mapper.UserMapper;
import com.amerd.schoolbook.exception.ExceptionHandling;
import com.amerd.schoolbook.security.provider.JWTProvider;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.mail.EmailService;
import com.amerd.schoolbook.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.amerd.schoolbook.common.constant.FileConstant.TEMP_PROFILE_IMAGE_BASE_ULR;
import static com.amerd.schoolbook.resource.ResourceMessage.INCOMING;

@Slf4j
@RestController
@RequestMapping(Endpoint.USER)
@RequiredArgsConstructor
public class UserResource extends ExceptionHandling {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @GetMapping(value = "/find/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> getByUsername(@PathVariable("user") String username) {
        log.info(String.format(INCOMING, "getByUsername", username));
        User user = userService.findByUsernameOrThrow(username);
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(user), "Retrieved user"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<CustomPage<User>>> getAll(Pageable pageable, HttpServletRequest request) {
        CustomPage<User> users = userService.getAllUsersPaged(pageable);
        return ResponseEntity.ok(new CustomHttpResponse<>(users, "All users"));
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userRegister(@RequestBody JsonNode request) throws IOException {
        log.info(String.format(INCOMING, "userRegister", request));
        User newUser = userService.register(request);
        String emailResponse = emailService
                .subject("Welcome!")
                .emailBody(String.format("Welcome to the app %s",
                        StringUtils.hasText(newUser.getFirstName()) ? newUser.getFirstName() : newUser.getUsername()))
                .recipient(newUser.getEmail())
                .sendEmail();
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(newUser), emailResponse));
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userCreate(@RequestBody JsonNode request) throws IOException {
        log.info(String.format(INCOMING, "userCreate", request));
        User user = userService.create(request);
        return ResponseEntity.ok(new CustomHttpResponse<>(
                userMapper.toResponseDto(user), String.format("Created new user. Public ID=[%s]", user.getPublicId())));
    }

    @PutMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userUpdate(
            @PathVariable("username") String username,
            @RequestBody JsonNode request)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        log.info(String.format(INCOMING, "edit", request));
        User updatedUser = userService.update(username, request);
        return ResponseEntity.ok(new CustomHttpResponse<>(
                userMapper.toResponseDto(updatedUser),
                String.format("User [%s] updated", updatedUser.getUsername())));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> login(@RequestBody JsonNode request) {
        log.info(String.format(INCOMING, "login", request));
        if (!request.hasNonNull(User.Fields.username)) throw new ValidationException("Username required");
        if (!request.hasNonNull(User.Fields.password)) throw new ValidationException("Password required");
        String username = request.get(User.Fields.username).asText();
        String password = request.get(User.Fields.password).asText();
        authenticate(username, password);
        User loggedUser = userService.findByUsernameOrThrow(username);
        UserPrincipal userPrincipal = new UserPrincipal(loggedUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(new CustomHttpResponse<>(
                userMapper.toResponseDto(loggedUser), "Logged in"), jwtHeader, HttpStatus.OK);
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        log.info(String.format(INCOMING, "delete", id));
        return ResponseEntity.ok(userService.delete(Long.parseLong(id)));
    }

    @PostMapping("/reset")
    public ResponseEntity<CustomHttpResponse<String>> resetPassword(@RequestParam String email) {
        log.info(String.format(INCOMING, "resetPassword", email));
        String newPassword = userService.resetPassword(email);
        String emailResponse = emailService
                .subject("Password Reset")
                .emailBody(String.format("Your new password is: %s", newPassword))
                .recipient(email)
                .sendEmail();
        return ResponseEntity.ok(new CustomHttpResponse<>("Password reset", emailResponse));
    }

    @PreAuthorize("#username.equalsIgnoreCase(authentication.name)")
    @PostMapping(value = "/profile/image/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> updateProfileImage(
            @PathVariable("username") String username,
            @RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        User user = userService.updateProfileImage(username, profileImage);
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(user), "Profile image updated"));
    }

    @GetMapping(value = "/profile/image/{username}.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getProfileImage(@PathVariable("username") String username) throws IOException {
        return userService.getProfileImage(username);
    }

    @GetMapping(value = "/profile/image/temp/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getTempProfileImage(@PathVariable("username") String username) {
        //   URL url = new URL(String.format(TEMP_PROFILE_IMAGE_BASE_ULR, username));
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try(InputStream inputStream = url.openStream()) {
//            int bytesRead;
//            byte[] chunk = new byte[1024];
//            while ((bytesRead = inputStream.read(chunk)) > 0) {
//                outputStream.write(chunk, 0, bytesRead);
//            }
//        }
        return restTemplate.getForObject(String.format(TEMP_PROFILE_IMAGE_BASE_ULR, username), byte[].class);

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

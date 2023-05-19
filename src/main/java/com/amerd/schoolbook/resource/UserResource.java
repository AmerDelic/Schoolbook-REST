package com.amerd.schoolbook.resource;

import com.amerd.schoolbook.common.constant.Endpoint;
import com.amerd.schoolbook.common.constant.SecurityConstant;
import com.amerd.schoolbook.common.response.CustomHttpResponse;
import com.amerd.schoolbook.common.response.CustomPage;
import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserCreationDto;
import com.amerd.schoolbook.domain.user.dto.UserRegistrationDto;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.UserUpdateDto;
import com.amerd.schoolbook.domain.user.dto.mapper.UserMapper;
import com.amerd.schoolbook.security.provider.JWTProvider;
import com.amerd.schoolbook.security.user.UserPrincipal;
import com.amerd.schoolbook.service.mail.EmailService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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

import javax.validation.ValidationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.amerd.schoolbook.common.constant.FileConstant.TEMP_PROFILE_IMAGE_BASE_ULR;

@RestController
@RequestMapping(Endpoint.USER)
@RequiredArgsConstructor
public class UserResource {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userCreate(@RequestBody UserCreationDto dto) throws IOException {
        User user = userService.create(dto);
        return ResponseEntity.ok(new CustomHttpResponse<>(
                userMapper.toResponseDto(user), String.format("Created new user. Public ID=[%s]", user.getPublicId())));
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userRegister(@RequestBody UserRegistrationDto dto) throws IOException {
        User newUser = userService.register(dto);
//        String emailResponse = emailService
//                .subject("Welcome!")
//                .emailBody(String.format("Welcome to the app %s",
//                        StringUtils.hasText(newUser.getFirstName()) ? newUser.getFirstName() : newUser.getUsername()))
//                .recipient(newUser.getEmail())
//                .sendEmail();
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(newUser), "email skipped"));
    }

    @GetMapping(value = "/find/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> getByUsername(@PathVariable("user") String username) {
        User user = userService.findByUsernameOrThrow(username);
        return ResponseEntity.ok(new CustomHttpResponse<>(userMapper.toResponseDto(user), "Retrieved user"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    //@PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<CustomHttpResponse<CustomPage<UserResponseDto>>> getAll(Pageable pageable) {
        CustomPage<UserResponseDto> users = userService.getAllUsersPaged(pageable);
        return ResponseEntity.ok(new CustomHttpResponse<>(users, "All users"));
    }

    @PutMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> userUpdate(
            @PathVariable("username") String username,
            @RequestBody UserUpdateDto dto)
            throws IOException {
        User updatedUser = userService.update(username, dto);
        return ResponseEntity.ok(new CustomHttpResponse<>(
                userMapper.toResponseDto(updatedUser),
                String.format("User [%s] updated", updatedUser.getUsername())));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> login(@RequestBody JsonNode request) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) { //TODO: try auto casting to long
        return ResponseEntity.ok(userService.delete(Long.parseLong(id)));
    }

    @PostMapping("/reset")
    public ResponseEntity<CustomHttpResponse<String>> resetPassword(@RequestParam String email) {
        String newPassword = userService.resetPassword(email);
        String emailResponse = emailService
                .subject("Password Reset")
                .emailBody(String.format("Your new password is: %s", newPassword))
                .recipient(email)
                .sendEmail();
        return ResponseEntity.ok(new CustomHttpResponse<>("Password reset", emailResponse));
    }

    //    @PreAuthorize("#username.equalsIgnoreCase(authentication.name)")
    @PostMapping(value = "/profile/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomHttpResponse<UserResponseDto>> updateProfileImage(
            @RequestParam("username") String username,
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

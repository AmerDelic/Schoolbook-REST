package com.amerd.schoolbook.domain.user.dto.mapper;

import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserCreationDto;
import com.amerd.schoolbook.domain.user.dto.UserRegistrationDto;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import com.amerd.schoolbook.domain.user.dto.UserUpdateDto;
import com.amerd.schoolbook.security.Role;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Locale;
@Validated
@Component
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "authorities", source = "role")
    UserResponseDto toResponseDto(User entity);

    @Mapping(source = "profileImage", target = "profileImageUrl", ignore = true)
    @Mapping(target = "publicId", expression = "java(generatePublicId())")
    @Mapping(target = "role", source = "role", defaultExpression = "java(com.amerd.schoolbook.security.Role.ROLE_USER.name())")
    @Mapping(target = "enabled", source = "enabled", defaultExpression = "java(true)")
    @Mapping(target = "nonLocked", source = "nonLocked", defaultExpression = "java(true)")
    User creationDtoToEntity(@Valid UserCreationDto dto);

    @Mapping(target = "password", source = "password", defaultExpression = "java(generatePassword())")
    @Mapping(target = "publicId", expression = "java(generatePublicId())")
    @Mapping(target = "role", expression = "java(com.amerd.schoolbook.security.Role.ROLE_USER.name())")
    @Mapping(target = "enabled", expression = "java(true)")
    @Mapping(target = "nonLocked", expression = "java(true)")
    User registrationDtoToEntity(@Valid UserRegistrationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "profileImage", target = "profileImageUrl", ignore = true)
    User updateEntity(UserUpdateDto dto, @MappingTarget User entity);

    default String[] mapAuthorities(String role) {
        return Role.valueOf(role).getAuthorities();
    }

    default String generatePassword() {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(8).toUpperCase(Locale.ROOT);
    }

    default String generatePublicId() {
        return new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(8).toUpperCase(Locale.ROOT);
    }
}

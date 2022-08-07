package com.amerd.schoolbook.domain.user.dto.mapper;

import com.amerd.schoolbook.domain.user.User;
import com.amerd.schoolbook.domain.user.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {


    UserResponseDto toResponseDto(User user);


}

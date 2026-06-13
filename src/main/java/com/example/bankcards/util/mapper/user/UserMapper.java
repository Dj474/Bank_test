package com.example.bankcards.util.mapper.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.util.enums.user.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    public abstract User toEntity(UserDtoIn dtoIn);

    public abstract UserDtoOut toDtoOut(User user);

    @Named("stringToRole")
    protected Role stringToRole(String role) {
        if (role == null) return Role.USER;
        return Role.valueOf(role.toUpperCase());
    }
}
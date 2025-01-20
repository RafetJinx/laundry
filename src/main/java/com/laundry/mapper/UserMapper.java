package com.laundry.mapper;

import com.laundry.dto.UserRequestDto;
import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;

import static com.laundry.util.DateTimeUtil.formatLocalDateTime;

public class UserMapper {

    public static User toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setDisplayName(dto.getDisplayName());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());
        return user;
    }

    public static UserResponseDto toResponseDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .role(entity.getRole())
                .createdAt(formatLocalDateTime(entity.getCreatedAt()))
                .updatedAt(formatLocalDateTime(entity.getUpdatedAt()))
                .build();
    }
}

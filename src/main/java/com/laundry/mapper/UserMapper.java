package com.laundry.mapper;

import com.laundry.dto.UserRequestDto;
import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class UserMapper {

    public static User toEntity(UserRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setDisplayName(requestDto.getDisplayName());
        user.setPassword(requestDto.getPassword());
        user.setEmail(requestDto.getEmail());
        user.setPhone(requestDto.getPhone());
        user.setAddress(requestDto.getAddress());
        user.setRole(requestDto.getRole());
        return user;
    }

    public static UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(formatLocalDateTime(user.getCreatedAt()))
                .updatedAt(formatLocalDateTime(user.getUpdatedAt()))
                .build();
    }
}

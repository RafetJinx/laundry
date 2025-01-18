package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponseDto {
    Long id;
    String username;
    String displayName;
    String email;
    String phone;
    String address;
    String role;
    String createdAt;
    String updatedAt;
}

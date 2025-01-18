package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponseDto {
    String token;
    String username;
}

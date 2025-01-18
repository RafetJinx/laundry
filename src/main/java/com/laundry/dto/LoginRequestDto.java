package com.laundry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginRequestDto {
    @NotBlank
    String username;

    @NotBlank
    String password;
}

package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

/**
 * After login, we typically return a JWT token to the client.
 */
@Value
@Builder
public class LoginResponseDto {
    String token;    // the JWT
    String username; // optional, if you want to return
    // maybe more fields (e.g., roles, userId, etc.)
}

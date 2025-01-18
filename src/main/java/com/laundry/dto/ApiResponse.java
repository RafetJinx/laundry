package com.laundry.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String result;
    private String message;
    private T data;

    // Helper methods for convenience
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}

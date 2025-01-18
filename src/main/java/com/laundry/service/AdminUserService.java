package com.laundry.service;

import com.laundry.dto.UserResponseDto;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface AdminUserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id) throws NotFoundException;

    UserResponseDto updateUserRole(Long id, String newRole) throws NotFoundException;
}

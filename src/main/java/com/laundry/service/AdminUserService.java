package com.laundry.service;

import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface AdminUserService {
    /**
     * Retrieves all {@link User} entities from the database and converts them
     * into {@link UserResponseDto} objects. Requires the {@code ADMIN} role.
     *
     * @return a list of {@link UserResponseDto} representing all users
     */
    List<UserResponseDto> getAllUsers();

    /**
     * Retrieves a single {@link User} by its unique identifier and converts it
     * into a {@link UserResponseDto}. Requires the {@code ADMIN} role.
     *
     * @param id the unique identifier of the user to retrieve
     * @return a {@link UserResponseDto} representing the requested user
     * @throws NotFoundException if the user with the specified {@code id} does not exist
     */
    UserResponseDto getUserById(Long id) throws NotFoundException;

    /**
     * Updates the role of a specific {@link User}. The new role must be either
     * {@code USER} or {@code ADMIN}, otherwise an {@link AccessDeniedException}
     * is thrown. Requires the {@code ADMIN} role.
     *
     * @param id      the unique identifier of the user whose role is to be updated
     * @param newRole the new role to assign to the user (expected values: {@code USER} or {@code ADMIN})
     * @return a {@link UserResponseDto} representing the user after the role update
     * @throws NotFoundException      if the user with the specified {@code id} does not exist
     * @throws AccessDeniedException if the requested {@code newRole} is not valid
     */
    UserResponseDto updateUserRole(Long id, String newRole) throws NotFoundException;
}

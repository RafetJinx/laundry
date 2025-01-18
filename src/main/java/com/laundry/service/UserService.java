package com.laundry.service;

import com.laundry.dto.UserRequestDto;
import com.laundry.dto.UserResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.NotFoundException;
import com.laundry.exception.UserAlreadyExistsException;

public interface UserService {

    UserResponseDto createUser(UserRequestDto userRequestDto) throws UserAlreadyExistsException;

    UserResponseDto updateUser(Long id,
                               UserRequestDto userRequestDto,
                               Long currentUserId,
                               String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException;

    UserResponseDto patchUser(Long id,
                              UserRequestDto userRequestDto,
                              Long currentUserId,
                              String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException;

    UserResponseDto getUserById(Long id,
                                Long currentUserId,
                                String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    void deleteUser(Long id,
                    Long currentUserId,
                    String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    // 1) For "forgot password" flow
    void initiatePasswordReset(String email) throws NotFoundException;

    // 2) For "reset password" with token
    void resetPasswordWithToken(String token, String newPassword) throws NotFoundException;

    // 3) For logged-in user wanting to change password
    void changePasswordLoggedIn(Long currentUserId, String oldPassword, String newPassword)
            throws NotFoundException, AccessDeniedException;

    boolean isResetTokenValid(String token);
}

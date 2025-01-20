package com.laundry.service;

import com.laundry.dto.UserRequestDto;
import com.laundry.dto.UserResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.InvalidEmailException;
import com.laundry.exception.NotFoundException;
import com.laundry.exception.UserAlreadyExistsException;

public interface UserService {

    /**
     * Creates a new user based on the provided DTO.
     * <ul>
     *   <li>Validates email format</li>
     *   <li>Checks if username or email already exist</li>
     *   <li>Encrypts the password before saving</li>
     * </ul>
     *
     * @param userRequestDto the {@link UserRequestDto} containing user creation data
     * @return a {@link UserResponseDto} representing the saved user
     * @throws UserAlreadyExistsException if the username or email already exists
     * @throws InvalidEmailException if the email address is invalid
     */
    UserResponseDto createUser(UserRequestDto userRequestDto) throws InvalidEmailException, UserAlreadyExistsException;

    /**
     * Fully updates a user's details. Overwrites all updatable fields with the
     * values provided in the DTO. Only the owner or an admin can perform this update.
     * <ul>
     *   <li>Checks if user exists by ID</li>
     *   <li>Validates ownership or admin role</li>
     *   <li>Ensures uniqueness of username and email if changed</li>
     *   <li>Encrypts the new password if provided</li>
     * </ul>
     *
     * @param id               the ID of the user to update
     * @param userRequestDto              the new user details in a {@link UserRequestDto}
     * @param currentUserId    the ID of the currently logged-in user
     * @param currentUserRole  the role of the currently logged-in user
     * @return a {@link UserResponseDto} representing the updated user
     * @throws NotFoundException if the user with the specified ID is not found
     * @throws UserAlreadyExistsException if the updated username or email already exists
     * @throws AccessDeniedException if the current user lacks permission to update this user
     */
    UserResponseDto updateUser(Long id,
                               UserRequestDto userRequestDto,
                               Long currentUserId,
                               String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException;

    /**
     * Partially updates a user's details. Only fields present (non-null) in the
     * DTO will be updated, leaving other fields unchanged. Only the owner or an
     * admin can perform this update.
     * <ul>
     *   <li>Checks if user exists by ID</li>
     *   <li>Validates ownership or admin role</li>
     *   <li>Ensures uniqueness of username and email if updated</li>
     *   <li>Encrypts the new password if provided</li>
     * </ul>
     *
     * @param id               the ID of the user to update
     * @param userRequestDto              the partial user details in a {@link UserRequestDto}
     * @param currentUserId    the ID of the currently logged-in user
     * @param currentUserRole  the role of the currently logged-in user
     * @return a {@link UserResponseDto} representing the patched user
     * @throws NotFoundException if the user with the specified ID is not found
     * @throws UserAlreadyExistsException if the updated username or email already exists
     * @throws AccessDeniedException if the current user lacks permission to patch this user
     */
    UserResponseDto patchUser(Long id,
                              UserRequestDto userRequestDto,
                              Long currentUserId,
                              String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException;

    /**
     * Retrieves a user by its unique identifier. Only the owner or an admin can
     * perform this operation.
     * <ul>
     *   <li>Checks if user exists by ID</li>
     *   <li>Validates ownership or admin role</li>
     * </ul>
     *
     * @param id               the ID of the user to retrieve
     * @param currentUserId    the ID of the currently logged-in user
     * @param currentUserRole  the role of the currently logged-in user
     * @return a {@link UserResponseDto} representing the found user
     * @throws NotFoundException if the user with the specified ID is not found
     * @throws AccessDeniedException if the current user lacks permission to view this user
     */
    UserResponseDto getUserById(Long id,
                                Long currentUserId,
                                String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Deletes a user by its unique identifier. Only the owner or an admin can
     * perform this operation.
     * <ul>
     *   <li>Checks if user exists by ID</li>
     *   <li>Validates ownership or admin role</li>
     *   <li>Removes the user record from the database</li>
     * </ul>
     *
     * @param id               the ID of the user to delete
     * @param currentUserId    the ID of the currently logged-in user
     * @param currentUserRole  the role of the currently logged-in user
     * @throws NotFoundException if the user with the specified ID is not found
     * @throws AccessDeniedException if the current user lacks permission to delete this user
     */
    void deleteUser(Long id,
                    Long currentUserId,
                    String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    /**
     * Initiates the password reset process for a user identified by email.
     * Generates a reset token, sets its expiration time, and sends an email
     * with reset instructions.
     *
     * @param email the email of the user requesting a password reset
     * @throws NotFoundException if no user with the given email is found
     */
    void initiatePasswordReset(String email) throws NotFoundException;

    /**
     * Resets a user's password using a valid reset token. If the token is invalid
     * or expired, this operation fails.
     * <ul>
     *   <li>Encrypts and updates the user's new password</li>
     *   <li>Clears the reset token and token expiration</li>
     * </ul>
     *
     * @param token       the reset token associated with the user
     * @param newPassword the new password to be set
     * @throws NotFoundException if the token is invalid or expired
     */
    void resetPasswordWithToken(String token, String newPassword) throws NotFoundException;

    /**
     * Changes the password for a user who is already logged in. Requires the
     * current (old) password for verification.
     *
     * @param currentUserId the ID of the currently logged-in user
     * @param oldPassword   the user's existing password
     * @param newPassword   the new password to be set
     * @throws NotFoundException if the user does not exist
     * @throws AccessDeniedException if the old password does not match the stored password
     */
    void changePasswordLoggedIn(Long currentUserId, String oldPassword, String newPassword)
            throws NotFoundException, AccessDeniedException;

    /**
     * Checks if a given reset token is valid. A token is considered valid if it
     * exists in the database and has not yet expired.
     *
     * @param token the reset token to validate
     * @return {@code true} if the token is valid and not expired; otherwise {@code false}
     */
    boolean isResetTokenValid(String token);
}

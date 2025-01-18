package com.laundry.service;

import com.laundry.dto.UserRequestDto;
import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.NotFoundException;
import com.laundry.exception.UserAlreadyExistsException;
import com.laundry.mapper.UserMapper;
import com.laundry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Override
    public UserResponseDto createUser(UserRequestDto dto) throws UserAlreadyExistsException {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + dto.getEmail());
        }

        User user = UserMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(Long id,
                                      UserRequestDto dto,
                                      Long currentUserId,
                                      String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!"ROLE_ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot update another user's profile");
        }

        if (!user.getUsername().equals(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + dto.getUsername());
        }
        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + dto.getEmail());
        }

        User temp = UserMapper.toEntity(dto);

        user.setUsername(temp.getUsername());
        user.setDisplayName(temp.getDisplayName());
        user.setEmail(temp.getEmail());
        user.setPhone(temp.getPhone());
        user.setAddress(temp.getAddress());
        user.setRole(temp.getRole());

        if (temp.getPassword() != null && !temp.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(temp.getPassword()));
        }

        userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto patchUser(Long id,
                                     UserRequestDto dto,
                                     Long currentUserId,
                                     String currentUserRole)
            throws NotFoundException, UserAlreadyExistsException, AccessDeniedException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!"ROLE_ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot patch another user's profile");
        }

        // Only update the non-null, changed fields
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new UserAlreadyExistsException("Username already exists: " + dto.getUsername());
            }
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new UserAlreadyExistsException("Email already in use: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }

    /**
     * Get user by ID (with self/admin check)
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id,
                                       Long currentUserId,
                                       String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!"ROLE_ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot view another user's profile");
        }

        return UserMapper.toResponseDto(user);
    }

    @Override
    public void deleteUser(Long id,
                           Long currentUserId,
                           String currentUserRole)
            throws NotFoundException, AccessDeniedException {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!"ROLE_ADMIN".equals(currentUserRole) && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot delete another user's profile");
        }

        userRepository.delete(user);
    }

    @Override
    public void initiatePasswordReset(String email) throws NotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No user found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);
        mailService.sendResetPasswordEmail(user, token);
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) throws NotFoundException {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid or expired reset token"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);

        userRepository.save(user);
    }

    @Override
    public void changePasswordLoggedIn(Long currentUserId, String oldPassword, String newPassword)
            throws NotFoundException, AccessDeniedException {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + currentUserId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AccessDeniedException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean isResetTokenValid(String token) {
        Optional<User> opt = userRepository.findByResetToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        User user = opt.get();
        if (user.getResetTokenExpiresAt() == null) {
            return false;
        }
        return user.getResetTokenExpiresAt().isAfter(LocalDateTime.now());
    }
}

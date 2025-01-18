package com.laundry.service;

import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException; // might still use it for "invalid role" logic
import com.laundry.exception.NotFoundException;
import com.laundry.mapper.UserMapper;
import com.laundry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Only admin can list all users.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Only admin can get user by ID.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserById(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return UserMapper.toResponseDto(user);
    }

    /**
     * Only admin can update a user's role.
     * If someone tries to set an "invalid" role, we throw an AccessDeniedException
     * or a custom exception to indicate it's not allowed.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateUserRole(Long id, String newRole) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        // Validate the new role. If you prefer, you can throw a different domain exception here.
        if (!"USER".equalsIgnoreCase(newRole) && !"ADMIN".equalsIgnoreCase(newRole)) {
            throw new AccessDeniedException("Invalid role assignment: " + newRole);
        }

        user.setRole(newRole.toUpperCase());
        userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }
}

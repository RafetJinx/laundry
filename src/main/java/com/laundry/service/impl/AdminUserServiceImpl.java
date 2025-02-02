package com.laundry.service.impl;

import com.laundry.dto.UserResponseDto;
import com.laundry.entity.User;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.NotFoundException;
import com.laundry.mapper.UserMapper;
import com.laundry.repository.UserRepository;
import com.laundry.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserById(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return UserMapper.toResponseDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateUserRole(Long id, String newRole) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!"USER".equalsIgnoreCase(newRole) && !"ADMIN".equalsIgnoreCase(newRole)) {
            throw new AccessDeniedException("Invalid role assignment: " + newRole);
        }

        user.setRole(newRole.toUpperCase());
        userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDto> getAllUsersByRole(String role, Pageable pageable) {
        Page<User> users = userRepository.findAllByRole(role.toUpperCase(), pageable);
        return users.map(UserMapper::toResponseDto);
    }
}

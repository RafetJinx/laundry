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
}

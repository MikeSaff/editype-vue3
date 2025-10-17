package com.editype.user.service;

import com.editype.exception.ResourceNotFoundException;
import com.editype.user.dto.UserRequestDTO;
import com.editype.user.dto.UserResponseDTO;
import com.editype.user.entity.User;
import com.editype.user.entity.UserRole;
import com.editype.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for User operations
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Get all users
     */
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID
     */
    public UserResponseDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponseDTO.fromEntity(user);
    }
    
    /**
     * Create new user with hashed password
     */
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + requestDTO.getEmail() + " already exists");
        }
        
        // Hash password using BCrypt
        String hashedPassword = BCrypt.hashpw(requestDTO.getPassword(), BCrypt.gensalt());
        
        // Set default role to AUTHOR if no roles provided
        Set<UserRole> roles = requestDTO.getRoles() != null && !requestDTO.getRoles().isEmpty() 
                ? requestDTO.getRoles() 
                : Set.of(UserRole.AUTHOR);
        
        User user = User.builder()
                .email(requestDTO.getEmail())
                .password(hashedPassword)
                .roles(roles)
                .build();
        
        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }
    
    /**
     * Update existing user
     */
    public UserResponseDTO updateUser(String id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update email if changed and not already taken
        if (!user.getEmail().equals(requestDTO.getEmail())) {
            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                throw new IllegalArgumentException("Email " + requestDTO.getEmail() + " is already in use");
            }
            user.setEmail(requestDTO.getEmail());
        }
        
        // Update password if provided
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            String hashedPassword = BCrypt.hashpw(requestDTO.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);
        }
        
        // Update roles if provided
        if (requestDTO.getRoles() != null) {
            user.setRoles(requestDTO.getRoles());
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(updatedUser);
    }
    
    /**
     * Delete user by ID
     */
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}





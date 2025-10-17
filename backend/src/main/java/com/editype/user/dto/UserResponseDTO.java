package com.editype.user.dto;

import com.editype.user.entity.User;
import com.editype.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for returning user data (without password)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    
    private String id;
    private String email;
    private Set<UserRole> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert User entity to ResponseDTO
     */
    public static UserResponseDTO fromEntity(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}





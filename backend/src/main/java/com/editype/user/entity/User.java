package com.editype.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the system
 * Поддерживает как локальных пользователей, так и пользователей из Editorum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String password; // hashed with BCrypt (для локальных пользователей)
    
    // Данные профиля
    private String firstName;
    private String lastName;
    
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();
    
    // Editorum интеграция
    private String editorumUserId; // ID пользователя в системе Editorum
    private String editorumAccessToken; // Токен доступа к Editorum API
    private String editorumRefreshToken; // Токен обновления для Editorum API
    private Boolean isEditorumUser = false; // Флаг - пользователь из Editorum
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}


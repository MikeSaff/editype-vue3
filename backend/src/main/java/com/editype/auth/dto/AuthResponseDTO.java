package com.editype.auth.dto;

import com.editype.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for authentication response with JWT token
 * Поддерживает как локальную авторизацию, так и Editorum OAuth2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private String token; // JWT токен для нашей системы
    private String email;
    private Set<UserRole> roles;
    
    // Editorum токены (для OAuth2 интеграции)
    private String editorumAccessToken;
    private String editorumRefreshToken;
    
    // Дополнительная информация о пользователе
    private String firstName;
    private String lastName;
    private Boolean isEditorumUser;
}


package com.editype.auth.service;

import com.editype.auth.dto.AuthResponseDTO;
import com.editype.auth.dto.LoginRequestDTO;
import com.editype.auth.dto.RegisterRequestDTO;
import com.editype.editorum.dto.EditorumUserDTO;
import com.editype.editorum.dto.OAuth2TokenResponse;
import com.editype.security.JwtUtil;
import com.editype.user.entity.User;
import com.editype.user.entity.UserRole;
import com.editype.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for authentication operations
 * Поддерживает как локальную авторизацию, так и OAuth2 с Editorum
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    /**
     * Login user and return JWT token (локальная авторизация - deprecated)
     */
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!BCrypt.checkpw(requestDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponseDTO.builder()
                .token(token)
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
    
    /**
     * Register new user and return JWT token (локальная регистрация - deprecated)
     */
    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + requestDTO.getEmail() + " already exists");
        }
        
        String hashedPassword = BCrypt.hashpw(requestDTO.getPassword(), BCrypt.gensalt());
        
        Set<UserRole> roles = requestDTO.getRoles() != null && !requestDTO.getRoles().isEmpty() 
                ? requestDTO.getRoles() 
                : Set.of(UserRole.AUTHOR);
        
        User user = User.builder()
                .email(requestDTO.getEmail())
                .password(hashedPassword)
                .roles(roles)
                .build();
        
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        return AuthResponseDTO.builder()
                .token(token)
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .build();
    }
    
    /**
     * Аутентификация через Editorum OAuth2 (единственная точка входа)
     */
    public AuthResponseDTO authenticateWithEditorum(EditorumUserDTO editorumUser, OAuth2TokenResponse tokenResponse) {
        String email = editorumUser.getEmail();
        
        // Ищем пользователя в нашей локальной базе
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // Создаем нового пользователя на основе данных из Editorum
            log.info("Creating new user from Editorum: {}", email);
            user = createUserFromEditorum(editorumUser);
        } else {
            // Обновляем существующего пользователя
            log.info("Updating existing user from Editorum: {}", email);
            updateUserFromEditorum(user, editorumUser);
        }
        
        // Сохраняем токены Editorum
        user.setEditorumAccessToken(tokenResponse.getAccessToken());
        user.setEditorumRefreshToken(tokenResponse.getRefreshToken());
        user = userRepository.save(user);
        
        // Генерируем JWT токен для нашей системы
        String jwtToken = jwtUtil.generateToken(user.getEmail());
        
        log.info("Successfully authenticated user via Editorum: {}", email);
        
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .roles(user.getRoles())
                .editorumAccessToken(tokenResponse.getAccessToken())
                .editorumRefreshToken(tokenResponse.getRefreshToken())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isEditorumUser(user.getIsEditorumUser())
                .build();
    }
    
    /**
     * Создание нового пользователя на основе данных из Editorum
     */
    private User createUserFromEditorum(EditorumUserDTO editorumUser) {
        Set<UserRole> roles = determineUserRoles(editorumUser);
        
        // Extract name from author.ru or author.en
        String fullName = extractFullName(editorumUser);
        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        User user = User.builder()
                .email(editorumUser.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .editorumUserId(editorumUser.getId().toString())
                .isEditorumUser(true)
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * Обновление существующего пользователя данными из Editorum
     */
    private void updateUserFromEditorum(User user, EditorumUserDTO editorumUser) {
        String fullName = extractFullName(editorumUser);
        String[] nameParts = fullName.split(" ", 2);
        user.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setEditorumUserId(editorumUser.getId().toString());
        user.setIsEditorumUser(true);
        
        // Обновляем роли на основе данных из Editorum
        Set<UserRole> newRoles = determineUserRoles(editorumUser);
        user.setRoles(newRoles);
    }
    
    /**
     * Extract full name from EditorumUserDTO
     */
    private String extractFullName(EditorumUserDTO editorumUser) {
        if (editorumUser.getAuthor() != null) {
            if (editorumUser.getAuthor().getRu() != null && editorumUser.getAuthor().getRu().containsKey("fio")) {
                return editorumUser.getAuthor().getRu().get("fio");
            }
            if (editorumUser.getAuthor().getEn() != null && editorumUser.getAuthor().getEn().containsKey("fio")) {
                return editorumUser.getAuthor().getEn().get("fio");
            }
        }
        return editorumUser.getEmail().split("@")[0]; // Fallback to email username
    }
    
    /**
     * Определение ролей пользователя на основе данных из Editorum
     */
    private Set<UserRole> determineUserRoles(EditorumUserDTO editorumUser) {
        Set<UserRole> roles = Set.of(UserRole.AUTHOR); // По умолчанию - автор
        
        // Здесь можно добавить логику определения ролей на основе данных из Editorum
        // Например, если в EditorumUserDTO есть поле roles или permissions
        
        return roles;
    }
    
    /**
     * Получение токена доступа Editorum для пользователя
     */
    public String getEditorumAccessToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null && user.getIsEditorumUser() && user.getEditorumAccessToken() != null) {
            return user.getEditorumAccessToken();
        }
        return null;
    }
    
    /**
     * Получение токена обновления Editorum для пользователя
     */
    public String getEditorumRefreshToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null && user.getIsEditorumUser() && user.getEditorumRefreshToken() != null) {
            return user.getEditorumRefreshToken();
        }
        return null;
    }
}


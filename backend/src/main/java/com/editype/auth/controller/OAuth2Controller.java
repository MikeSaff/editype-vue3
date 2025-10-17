package com.editype.auth.controller;

import com.editype.editorum.client.EditorumOAuth2Client;
import com.editype.editorum.client.EditorumApiClient;
import com.editype.editorum.dto.OAuth2TokenResponse;
import com.editype.editorum.dto.EditorumUserDTO;
import com.editype.auth.service.AuthService;
import com.editype.auth.dto.AuthResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for OAuth2 authorization with Editorum (единственная точка входа)
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OAuth2Controller {
    
    private final EditorumOAuth2Client oAuth2Client;
    private final EditorumApiClient editorumApiClient;
    private final AuthService authService;
    
    /**
     * Get authorization URL - перенаправляет на Editorum для авторизации
     */
    @GetMapping("/authorize-url")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        String url = oAuth2Client.getAuthorizationUrl();
        log.info("Generated authorization URL for Editorum OAuth2");
        return ResponseEntity.ok(Map.of("url", url));
    }
    
    /**
     * Exchange code for token and create/update user session
     * Вызывается после возврата с Editorum OAuth2
     */
    @PostMapping("/callback")
    public ResponseEntity<AuthResponseDTO> handleCallback(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            log.info("Processing OAuth2 callback with code");
            
            // Получаем токены от Editorum
            OAuth2TokenResponse tokenResponse = oAuth2Client.getAccessToken(code);
            
            // Получаем профиль пользователя из Editorum
            EditorumUserDTO editorumUser = editorumApiClient.getCurrentUser(
                "Bearer " + tokenResponse.getAccessToken()
            );
            
            // Создаем или обновляем пользователя в нашей системе
            AuthResponseDTO authResponse = authService.authenticateWithEditorum(editorumUser, tokenResponse);
            
            log.info("Successfully authenticated user: {}", editorumUser.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 callback", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<OAuth2TokenResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            OAuth2TokenResponse token = oAuth2Client.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check if user is authenticated
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Здесь можно проверить валидность токена
                return ResponseEntity.ok(Map.of("authenticated", true));
            }
            return ResponseEntity.ok(Map.of("authenticated", false));
        } catch (Exception e) {
            log.error("Error checking auth status", e);
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}


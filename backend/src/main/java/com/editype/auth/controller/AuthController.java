package com.editype.auth.controller;

import com.editype.auth.dto.AuthResponseDTO;
import com.editype.auth.dto.LoginRequestDTO;
import com.editype.auth.dto.RegisterRequestDTO;
import com.editype.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.login(requestDTO));
    }
    
    /**
     * Register endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.register(requestDTO));
    }
}





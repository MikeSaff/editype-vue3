package com.editype.editorum.client;

import com.editype.editorum.config.EditorumConfig;
import com.editype.editorum.dto.OAuth2TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth2 client for Editorum authorization
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EditorumOAuth2Client {
    
    private final EditorumConfig config;
    private final RestTemplate editorumRestTemplate;
    
    /**
     * Get authorization URL for user
     */
    public String getAuthorizationUrl() {
        return String.format("%s/oauth/v2/auth?client_id=%s&redirect_uri=%s&response_type=code",
                config.getBaseUrl(),
                config.getClientId(),
                config.getRedirectUri());
    }
    
    /**
     * Exchange authorization code for access token
     * Must be called within 60 seconds of receiving code
     */
    public OAuth2TokenResponse getAccessToken(String code) {
        String url = config.getBaseUrl() + "/oauth/v2/token";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", config.getRedirectUri());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        ResponseEntity<OAuth2TokenResponse> response = editorumRestTemplate.exchange(
                url, HttpMethod.POST, entity, OAuth2TokenResponse.class);
        
        return response.getBody();
    }
    
    /**
     * Refresh access token using refresh token
     */
    public OAuth2TokenResponse refreshAccessToken(String refreshToken) {
        String url = config.getBaseUrl() + "/oauth/v2/token";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("refresh_token", refreshToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        ResponseEntity<OAuth2TokenResponse> response = editorumRestTemplate.exchange(
                url, HttpMethod.POST, entity, OAuth2TokenResponse.class);
        
        return response.getBody();
    }
}





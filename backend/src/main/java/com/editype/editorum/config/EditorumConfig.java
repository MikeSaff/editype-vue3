package com.editype.editorum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Editorum integration
 */
@Configuration
public class EditorumConfig {
    
    @Value("${editorum.api.base-url:http://znan.io}")
    private String baseUrl;
    
    @Value("${editorum.oauth.client-id:}")
    private String clientId;
    
    @Value("${editorum.oauth.client-secret:}")
    private String clientSecret;
    
    @Value("${editorum.oauth.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;
    
    @Bean
    public RestTemplate editorumRestTemplate() {
        return new RestTemplate();
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
}





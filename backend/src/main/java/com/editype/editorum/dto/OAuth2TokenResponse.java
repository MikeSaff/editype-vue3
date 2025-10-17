package com.editype.editorum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 token response from Editorum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    private String scope;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
}





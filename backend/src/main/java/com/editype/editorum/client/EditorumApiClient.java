package com.editype.editorum.client;

import com.editype.editorum.config.EditorumConfig;
import com.editype.editorum.dto.EditorumArticleDTO;
import com.editype.editorum.dto.EditorumDocumentTypeDTO;
import com.editype.editorum.dto.EditorumJournalDTO;
import com.editype.editorum.dto.EditorumUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST client for Editorum API
 * Handles all interactions with Editorum backend
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EditorumApiClient {
    
    private final EditorumConfig config;
    private final RestTemplate editorumRestTemplate;
    
    /**
     * Get current user profile
     * GET /rest/api/user
     */
    public EditorumUserDTO getCurrentUser(String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/user";
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<EditorumUserDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, EditorumUserDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Get article by ID
     * GET /rest/api/articles/{id}
     */
    public EditorumArticleDTO getArticle(String accessToken, String id) {
        String url = config.getBaseUrl() + "/rest/api/articles/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<EditorumArticleDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, EditorumArticleDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Get user articles
     * GET /rest/api/articles/user
     */
    public List<EditorumArticleDTO> getUserArticles(String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/articles/user";
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, List.class);
        
        // TODO: Properly convert response to List<EditorumArticleDTO>
        return List.of();
    }
    
    /**
     * Create new article
     * POST /rest/api/articles
     */
    public EditorumArticleDTO createArticle(String accessToken, Object request) {
        String url = config.getBaseUrl() + "/rest/api/articles";
        HttpHeaders headers = createHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        ResponseEntity<EditorumArticleDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.POST, entity, EditorumArticleDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Update existing article
     * PUT /rest/api/articles/{id}
     */
    public EditorumArticleDTO updateArticle(String accessToken, String id, Object request) {
        String url = config.getBaseUrl() + "/rest/api/articles/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        ResponseEntity<EditorumArticleDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.PUT, entity, EditorumArticleDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Delete article
     * DELETE /rest/api/articles/{id}
     */
    public void deleteArticle(String accessToken, String id) {
        String url = config.getBaseUrl() + "/rest/api/articles/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        editorumRestTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
    
    /**
     * Get all journals
     * GET /rest/api/journals
     */
    public Map<String, EditorumJournalDTO> getAllJournals(String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/journals";
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
        
        return response.getBody();
    }
    
    /**
     * Get journal by ID
     * GET /rest/api/journals/{id}
     */
    public EditorumJournalDTO getJournal(Long id, String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/journals/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<EditorumJournalDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, EditorumJournalDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Upload file attachment
     * POST /rest/api/attachfile/{type}/{id}
     */
    public Map<String, Object> attachFile(String type, Long id, byte[] fileData, 
                                         String filename, Integer kind, String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/attachfile/" + type + "/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        // TODO: Implement multipart file upload
        log.warn("File attachment not fully implemented yet");
        return new HashMap<>();
    }
    
    /**
     * Get files attached to publication
     * GET /rest/api/files/{type}/{id}
     */
    public Map<String, Object> getFiles(String type, Long id, String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/files/" + type + "/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
        
        return response.getBody();
    }
    
    /**
     * Get available document types
     * GET /rest/api/document-types
     */
    public List<EditorumDocumentTypeDTO> getDocumentTypes(String accessToken) {
        String url = config.getBaseUrl() + "/rest/api/document-types";
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, List.class);
        
        // TODO: Properly convert response to List<EditorumDocumentTypeDTO>
        return List.of();
    }
    
    /**
     * Get document type by ID
     * GET /rest/api/document-types/{id}
     */
    public EditorumDocumentTypeDTO getDocumentType(String accessToken, String id) {
        String url = config.getBaseUrl() + "/rest/api/document-types/" + id;
        HttpHeaders headers = createHeaders(accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<EditorumDocumentTypeDTO> response = editorumRestTemplate.exchange(
                url, HttpMethod.GET, entity, EditorumDocumentTypeDTO.class);
        
        return response.getBody();
    }
    
    /**
     * Create authorization headers with Bearer token
     */
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }
}


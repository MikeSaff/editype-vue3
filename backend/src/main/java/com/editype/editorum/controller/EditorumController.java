package com.editype.editorum.controller;

import com.editype.editorum.client.EditorumApiClient;
import com.editype.editorum.dto.EditorumJournalDTO;
import com.editype.editorum.dto.EditorumUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Editorum integration
 * Proxies requests to Editorum API
 */
@RestController
@RequestMapping("/api/editorum")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EditorumController {
    
    private final EditorumApiClient editorumApiClient;
    
    /**
     * Get current user profile from Editorum
     */
    @GetMapping("/user")
    public ResponseEntity<EditorumUserDTO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        String accessToken = extractToken(authHeader);
        EditorumUserDTO user = editorumApiClient.getCurrentUser(accessToken);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all journals
     */
    @GetMapping("/journals")
    public ResponseEntity<Map<String, EditorumJournalDTO>> getAllJournals(
            @RequestHeader("Authorization") String authHeader) {
        
        String accessToken = extractToken(authHeader);
        Map<String, EditorumJournalDTO> journals = editorumApiClient.getAllJournals(accessToken);
        return ResponseEntity.ok(journals);
    }
    
    /**
     * Get journal by ID
     */
    @GetMapping("/journals/{id}")
    public ResponseEntity<EditorumJournalDTO> getJournal(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        String accessToken = extractToken(authHeader);
        EditorumJournalDTO journal = editorumApiClient.getJournal(id, accessToken);
        return ResponseEntity.ok(journal);
    }
    
    /**
     * Get files attached to publication
     */
    @GetMapping("/files/{type}/{id}")
    public ResponseEntity<Map<String, Object>> getFiles(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        String accessToken = extractToken(authHeader);
        Map<String, Object> files = editorumApiClient.getFiles(type, id, accessToken);
        return ResponseEntity.ok(files);
    }
    
    /**
     * Extract Bearer token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }
}





package com.editype.article.controller;

import com.editype.article.entity.Article;
import com.editype.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Article operations (Znan.io)
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * Get all articles
     */
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }
    
    /**
     * Get article by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "en") String lang) {
        Article article = articleService.getArticleById(id);
        return ResponseEntity.ok(article);
    }
    
    /**
     * Load article from Editorum
     */
    @PostMapping("/load/{editorumId}")
    public ResponseEntity<Article> loadFromEditorum(
            @PathVariable Long editorumId,
            @RequestHeader("Authorization") String authHeader) {
        
        String accessToken = extractToken(authHeader);
        Article article = articleService.loadFromEditorum(editorumId, accessToken);
        return ResponseEntity.ok(article);
    }
    
    /**
     * Save article (locally and to Editorum)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Article> saveArticle(
            @PathVariable String id,
            @RequestBody Article article,
            @RequestHeader("Authorization") String authHeader) {
        
        article.setId(id);
        Article saved = articleService.saveArticle(article);
        
        // Optionally sync to Editorum
        String accessToken = extractToken(authHeader);
        if (article.getEditorumId() != null) {
            saved = articleService.saveToEditorum(id, accessToken);
        }
        
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Acquire lock for editing
     */
    @PostMapping("/{id}/lock")
    public ResponseEntity<Map<String, Object>> acquireLock(
            @PathVariable String id,
            @RequestParam String userEmail) {
        
        boolean acquired = articleService.acquireLock(id, userEmail);
        return ResponseEntity.ok(Map.of(
                "success", acquired,
                "message", acquired ? "Lock acquired" : "Article is locked by another user"
        ));
    }
    
    /**
     * Release lock
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<Void> releaseLock(
            @PathVariable String id,
            @RequestParam String userEmail) {
        
        articleService.releaseLock(id, userEmail);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete article
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Extract Bearer token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}





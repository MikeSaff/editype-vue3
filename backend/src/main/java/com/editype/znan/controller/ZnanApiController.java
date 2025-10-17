package com.editype.znan.controller;

import com.editype.article.entity.Article;
import com.editype.article.service.ArticleService;
import com.editype.auth.service.AuthService;
import com.editype.editorum.client.EditorumApiClient;
import com.editype.editorum.dto.EditorumArticleDTO;
import com.editype.znan.dto.ZnanArticleResponseDTO;
import com.editype.znan.dto.ZnanCreateArticleRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для Znan.io API
 * Обеспечивает тонкий слой между фронтендом и Editorum API
 */
@Slf4j
@RestController
@RequestMapping("/api/znan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ZnanApiController {
    
    private final ArticleService articleService;
    private final AuthService authService;
    private final EditorumApiClient editorumApiClient;
    
    /**
     * Получение списка статей пользователя
     * Синхронизируется с Editorum API
     */
    @GetMapping("/articles")
    public ResponseEntity<List<ZnanArticleResponseDTO>> getUserArticles(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Loading articles for user: {}", userEmail);
            
            // Получаем токен Editorum для текущего пользователя
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                log.warn("No Editorum token found for user: {}", userEmail);
                return ResponseEntity.ok(List.of());
            }
            
            // Загружаем статьи из Editorum API
            List<EditorumArticleDTO> editorumArticles = editorumApiClient.getUserArticles(
                "Bearer " + editorumToken
            );
            
            // Конвертируем в формат Znan.io
            List<ZnanArticleResponseDTO> articles = editorumArticles.stream()
                .map(this::convertToZnanArticle)
                .collect(Collectors.toList());
            
            log.info("Loaded {} articles for user: {}", articles.size(), userEmail);
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error loading user articles", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получение статьи по ID с указанным языком
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<ZnanArticleResponseDTO> getArticle(
            @PathVariable String id,
            @RequestParam(defaultValue = "ru") String lang,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Loading article {} with language {} for user: {}", id, lang, userEmail);
            
            // Получаем токен Editorum
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Загружаем статью из Editorum API
            EditorumArticleDTO editorumArticle = editorumApiClient.getArticle(
                "Bearer " + editorumToken, id
            );
            
            // Конвертируем в формат Znan.io
            ZnanArticleResponseDTO article = convertToZnanArticle(editorumArticle);
            
            return ResponseEntity.ok(article);
            
        } catch (Exception e) {
            log.error("Error loading article {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Создание новой статьи
     */
    @PostMapping("/articles")
    public ResponseEntity<ZnanArticleResponseDTO> createArticle(
            @RequestBody ZnanCreateArticleRequestDTO request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Creating new article for user: {}", userEmail);
            
            // Получаем токен Editorum
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Создаем статью в Editorum API
            EditorumArticleDTO editorumArticle = editorumApiClient.createArticle(
                "Bearer " + editorumToken, request
            );
            
            // Конвертируем в формат Znan.io
            ZnanArticleResponseDTO article = convertToZnanArticle(editorumArticle);
            
            log.info("Created article {} for user: {}", article.getId(), userEmail);
            return ResponseEntity.ok(article);
            
        } catch (Exception e) {
            log.error("Error creating article", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Обновление статьи
     */
    @PutMapping("/articles/{id}")
    public ResponseEntity<ZnanArticleResponseDTO> updateArticle(
            @PathVariable String id,
            @RequestBody ZnanArticleResponseDTO request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Updating article {} for user: {}", id, userEmail);
            
            // Получаем токен Editorum
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Обновляем статью в Editorum API
            EditorumArticleDTO editorumArticle = editorumApiClient.updateArticle(
                "Bearer " + editorumToken, id, request
            );
            
            // Конвертируем в формат Znan.io
            ZnanArticleResponseDTO article = convertToZnanArticle(editorumArticle);
            
            log.info("Updated article {} for user: {}", id, userEmail);
            return ResponseEntity.ok(article);
            
        } catch (Exception e) {
            log.error("Error updating article {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Удаление статьи
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Deleting article {} for user: {}", id, userEmail);
            
            // Получаем токен Editorum
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Удаляем статью в Editorum API
            editorumApiClient.deleteArticle("Bearer " + editorumToken, id);
            
            log.info("Deleted article {} for user: {}", id, userEmail);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error deleting article {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Конвертация EditorumArticleDTO в ZnanArticleResponseDTO
     */
    private ZnanArticleResponseDTO convertToZnanArticle(EditorumArticleDTO editorumArticle) {
        // Build multilingual maps
        Map<String, String> titleMap = new HashMap<>();
        Map<String, String> abstractMap = new HashMap<>();
        Map<String, String> keywordsMap = new HashMap<>();
        Map<String, String> textMap = new HashMap<>();
        
        if (editorumArticle.getRu() != null && editorumArticle.getRu().containsKey("article")) {
            EditorumArticleDTO.MetadataFields ru = editorumArticle.getRu().get("article");
            titleMap.put("ru", ru.getTitle());
            abstractMap.put("ru", ru.getAnnotation());
            keywordsMap.put("ru", ru.getKeywords());
        }
        
        if (editorumArticle.getEn() != null && editorumArticle.getEn().containsKey("article")) {
            EditorumArticleDTO.MetadataFields en = editorumArticle.getEn().get("article");
            titleMap.put("en", en.getTitle());
            abstractMap.put("en", en.getAnnotation());
            keywordsMap.put("en", en.getKeywords());
        }
        
        // Text is single field
        textMap.put("default", editorumArticle.getText());
        
        String statusStr = editorumArticle.getStatuses() != null ? 
            editorumArticle.getStatuses().getStatus().toString() : "draft";
        
        return ZnanArticleResponseDTO.builder()
                .id(editorumArticle.getId().toString())
                .title(titleMap)
                .abstract_(abstractMap)
                .text(textMap)
                .keywords(keywordsMap)
                .status(statusStr)
                .build();
    }
}




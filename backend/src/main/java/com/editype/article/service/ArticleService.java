package com.editype.article.service;

import com.editype.article.entity.Article;
import com.editype.article.entity.ArticleMetadata;
import com.editype.article.repository.ArticleRepository;
import com.editype.editorum.client.EditorumApiClient;
import com.editype.editorum.dto.EditorumArticleDTO;
import com.editype.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Article operations
 * Integrates with Editorum API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    private final EditorumApiClient editorumApiClient;
    
    /**
     * Get all articles
     */
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }
    
    /**
     * Get article by ID
     */
    public Article getArticleById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
    }
    
    /**
     * Get article by ID (alias for ExportController)
     */
    public Article getById(String id) {
        return getArticleById(id);
    }
    
    /**
     * Load article from Editorum by Editorum ID
     */
    public Article loadFromEditorum(Long editorumId, String accessToken) {
        // Check if already synced
        Article existing = articleRepository.findByEditorumId(editorumId).orElse(null);
        
        // Fetch from Editorum
        EditorumArticleDTO editorumArticle = editorumApiClient.getArticle(accessToken, editorumId.toString());
        
        // Map to our Article entity
        Article article = existing != null ? existing : new Article();
        article.setEditorumId(editorumId);
        article.setText(editorumArticle.getText());
        article.setDoi(editorumArticle.getDoi());
        article.setFirstPage(editorumArticle.getFirstPage());
        article.setLastPage(editorumArticle.getLastPage());
        article.setLanguages(editorumArticle.getLanguages());
        article.setAuthorIds(editorumArticle.getAuthors());
        article.setRubricId(editorumArticle.getRubric() != null ? editorumArticle.getRubric().getId() : null);
        
        // Map metadata
        Map<String, ArticleMetadata> metadata = new HashMap<>();
        if (editorumArticle.getRu() != null) {
            EditorumArticleDTO.MetadataFields ruMeta = editorumArticle.getRu().get("article");
            if (ruMeta != null) {
                metadata.put("ru", ArticleMetadata.builder()
                        .title(ruMeta.getTitle())
                        .annotation(ruMeta.getAnnotation())
                        .keywords(ruMeta.getKeywords())
                        .build());
            }
        }
        if (editorumArticle.getEn() != null) {
            EditorumArticleDTO.MetadataFields enMeta = editorumArticle.getEn().get("article");
            if (enMeta != null) {
                metadata.put("en", ArticleMetadata.builder()
                        .title(enMeta.getTitle())
                        .annotation(enMeta.getAnnotation())
                        .keywords(enMeta.getKeywords())
                        .build());
            }
        }
        article.setMetadata(metadata);
        
        // Map references
        // TODO: Map references from Editorum format
        
        // Save to local DB
        return articleRepository.save(article);
    }
    
    /**
     * Save article back to Editorum
     */
    public Article saveToEditorum(String id, String accessToken) {
        Article article = getArticleById(id);
        
        // Build Editorum DTO
        EditorumArticleDTO editorumDto = new EditorumArticleDTO();
        editorumDto.setId(article.getEditorumId());
        editorumDto.setText(article.getText());
        editorumDto.setDoi(article.getDoi());
        editorumDto.setFirstPage(article.getFirstPage());
        editorumDto.setLastPage(article.getLastPage());
        editorumDto.setLanguages(article.getLanguages());
        editorumDto.setAuthors(article.getAuthorIds());
        
        // Map metadata back
        Map<String, EditorumArticleDTO.MetadataFields> ru = new HashMap<>();
        Map<String, EditorumArticleDTO.MetadataFields> en = new HashMap<>();
        
        if (article.getMetadata().containsKey("ru")) {
            ArticleMetadata ruMeta = article.getMetadata().get("ru");
            EditorumArticleDTO.MetadataFields ruFields = new EditorumArticleDTO.MetadataFields();
            ruFields.setTitle(ruMeta.getTitle());
            ruFields.setAnnotation(ruMeta.getAnnotation());
            ruFields.setKeywords(ruMeta.getKeywords());
            ru.put("ru", ruFields);
        }
        
        if (article.getMetadata().containsKey("en")) {
            ArticleMetadata enMeta = article.getMetadata().get("en");
            EditorumArticleDTO.MetadataFields enFields = new EditorumArticleDTO.MetadataFields();
            enFields.setTitle(enMeta.getTitle());
            enFields.setAnnotation(enMeta.getAnnotation());
            enFields.setKeywords(enMeta.getKeywords());
            en.put("en", enFields);
        }
        
        editorumDto.setRu(ru);
        editorumDto.setEn(en);
        
        // Save to Editorum
        if (article.getEditorumId() != null) {
            editorumApiClient.updateArticle(accessToken, article.getEditorumId().toString(), editorumDto);
        } else {
            EditorumArticleDTO created = editorumApiClient.createArticle(accessToken, editorumDto);
            article.setEditorumId(created.getId());
        }
        
        return articleRepository.save(article);
    }
    
    /**
     * Create or update article locally
     */
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }
    
    /**
     * Acquire lock for editing
     */
    public boolean acquireLock(String id, String userEmail) {
        Article article = getArticleById(id);
        
        // Check if already locked
        if (article.getLockedBy() != null) {
            // Check if lock is stale (older than 5 minutes)
            if (article.getLockedAt() != null && 
                article.getLockedAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
                return false; // Still locked by someone else
            }
        }
        
        // Acquire lock
        article.setLockedBy(userEmail);
        article.setLockedAt(LocalDateTime.now());
        articleRepository.save(article);
        
        return true;
    }
    
    /**
     * Release lock
     */
    public void releaseLock(String id, String userEmail) {
        Article article = getArticleById(id);
        
        if (userEmail.equals(article.getLockedBy())) {
            article.setLockedBy(null);
            article.setLockedAt(null);
            articleRepository.save(article);
        }
    }
    
    /**
     * Delete article
     */
    public void deleteArticle(String id) {
        articleRepository.deleteById(id);
    }
}





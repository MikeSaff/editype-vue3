package com.editype.article.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Article entity compatible with Editorum API
 * Mapped to Editorum's /rest/api/articles structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "articles")
public class Article {
    
    @Id
    private String id;
    
    // Editorum article ID (если статья из Editorum)
    private Long editorumId;
    
    // Multilingual metadata: ru, en, cn, etc
    // Maps to article[ru][title], article[en][title], etc
    @Builder.Default
    private Map<String, ArticleMetadata> metadata = new HashMap<>();
    
    // Main text - SINGLE field (as in Editorum)
    // Note: В Editorum это article[text] - одно поле
    // Для Znan.io можем хранить HTML/PM-JSON
    private String text;
    
    // Optional: Rich text in ProseMirror JSON format for editor
    private String pmJson;
    
    // Структура документа в виде абзацев (для WYSIWYG редактора)
    @Builder.Default
    private List<ArticleParagraph> paragraphs = new ArrayList<>();
    
    // ID используемого шаблона
    private String templateId;
    
    // Author IDs (as in Editorum)
    @Builder.Default
    private List<Long> authorIds = new ArrayList<>();
    
    // References with multilingual text
    @Builder.Default
    private List<ArticleReference> references = new ArrayList<>();
    
    // Journal rubric ID
    private Long rubricId;
    
    // DOI
    private String doi;
    
    // Page numbers
    private Integer firstPage;
    private Integer lastPage;
    
    // Supported languages
    @Builder.Default
    private List<String> languages = new ArrayList<>();
    
    // Statuses (compatible with Editorum)
    private Boolean hasPublished;
    private Boolean hasReviewers;
    private Integer status; // Editorum status code
    
    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Soft lock for sequential editing (before CRDT)
    private String lockedBy; // user email
    private LocalDateTime lockedAt;
}





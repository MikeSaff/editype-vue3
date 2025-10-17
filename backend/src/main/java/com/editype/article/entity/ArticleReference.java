package com.editype.article.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Reference entry with multilingual text
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleReference {
    
    private String id;
    
    // {"ru": "Текст списка", "en": "Reference text"}
    @Builder.Default
    private Map<String, String> text = new HashMap<>();
    
    // Optional: DOI, URL, etc for structured references
    private String doi;
    private String url;
}





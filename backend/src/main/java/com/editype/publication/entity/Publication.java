package com.editype.publication.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Publication entity with multilingual support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "publications")
public class Publication {
    
    @Id
    private String id;
    
    // Multilingual titles: {"ru": "Заголовок", "en": "Title", "cn": "标题"}
    @Builder.Default
    private Map<String, String> titles = new HashMap<>();
    
    // Multilingual metadata: {"ru": {...}, "en": {...}, "cn": {...}}
    @Builder.Default
    private Map<String, Map<String, String>> metadata = new HashMap<>();
    
    // Multilingual texts: {"ru": "Текст публикации", "en": "Publication text", "cn": "出版物文本"}
    @Builder.Default
    private Map<String, String> texts = new HashMap<>();
    
    private String doi; // Digital Object Identifier
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}





package com.editype.znan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для ответа API Znan.io
 * Представляет статью в формате, удобном для фронтенда
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZnanArticleResponseDTO {
    
    private String id;
    
    // Мультиязычные поля
    private Map<String, String> title; // { "ru": "Заголовок", "en": "Title" }
    private Map<String, String> abstract_; // { "ru": "Аннотация", "en": "Abstract" }
    private Map<String, String> text; // { "ru": "Текст статьи", "en": "Article text" }
    private Map<String, String> keywords; // { "ru": "ключевые слова", "en": "keywords" }
    
    // Метаданные
    private String status; // draft, review, published, archived
    private String doi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Авторы (если нужно)
    private String authors;
    
    // Журнал/коллекция
    private String journalId;
    private String journalName;
    
    // Дополнительные поля для редактора
    private String documentType; // Тип документа из Editorum
    private String templateId; // ID используемого шаблона
    
    // Статистика
    private Integer wordCount;
    private Integer pageCount;
    
    // Доступные языки
    private String[] availableLanguages;
}




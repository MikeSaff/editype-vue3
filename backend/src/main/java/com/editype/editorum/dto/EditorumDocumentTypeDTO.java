package com.editype.editorum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для типа документа из Editorum API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorumDocumentTypeDTO {
    
    private String id;
    private String name;
    private String description;
    private String category;
    private boolean active;
    
    // Дополнительные поля для шаблонов
    private String[] availableTemplates;
    private String defaultTemplateId;
    
    // Настройки по умолчанию
    private String defaultLanguage;
    private String[] supportedLanguages;
}




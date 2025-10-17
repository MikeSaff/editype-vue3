package com.editype.znan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа с типом документа в Znan.io API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZnanDocumentTypeResponseDTO {
    
    private String id;
    private String name;
    private String description;
    private String category;
    private boolean active;
    
    // Доступные шаблоны
    private String[] availableTemplates;
    private String defaultTemplateId;
    
    // Языковые настройки
    private String defaultLanguage;
    private String[] supportedLanguages;
}




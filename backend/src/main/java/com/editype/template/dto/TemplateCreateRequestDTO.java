package com.editype.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * DTO для создания нового шаблона
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateRequestDTO {
    
    private String name;
    private String description;
    private String documentTypeId;
    private String category;
    
    // Контент
    private String content;
    private Map<String, String> metadata;
    
    // Стили
    private String cssStyles;
    private List<ParagraphStyleRequestDTO> paragraphStyles;
    
    // Настройки
    private boolean active;
    private boolean isDefault;
    private String version;
    
    // Загружаемый файл
    private MultipartFile file;
    private String fileType; // html, docx, latex
    
    /**
     * DTO для стилей абзацев при создании
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParagraphStyleRequestDTO {
        private String name;
        private String type;
        private String cssClass;
        private String cssStyles;
        private Map<String, String> properties;
        private boolean isDefault;
        private int order;
    }
}




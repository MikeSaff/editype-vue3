package com.editype.template.dto;

import com.editype.template.entity.DocumentTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO для ответа с информацией о шаблоне
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponseDTO {
    
    private String id;
    private String name;
    private String description;
    private String documentTypeId;
    private String category;
    
    // Контент (может быть скрыт для списков)
    private String content;
    private Map<String, String> metadata;
    
    // Стили
    private String cssStyles;
    private List<ParagraphStyleDTO> paragraphStyles;
    
    // Настройки
    private boolean active;
    private boolean isDefault;
    private String version;
    
    // Файловая информация
    private String originalFileName;
    private String fileType;
    
    // Автор
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * DTO для стилей абзацев
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParagraphStyleDTO {
        private String id;
        private String name;
        private String type;
        private String cssClass;
        private String cssStyles;
        private Map<String, String> properties;
        private boolean isDefault;
        private int order;
    }
    
    /**
     * Конвертация из сущности
     */
    public static TemplateResponseDTO fromEntity(DocumentTemplate template) {
        return TemplateResponseDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .documentTypeId(template.getDocumentTypeId())
                .category(template.getCategory())
                .content(template.getContent())
                .metadata(template.getMetadata())
                .cssStyles(template.getCssStyles())
                .paragraphStyles(convertParagraphStyles(template.getParagraphStyles()))
                .active(template.isActive())
                .isDefault(template.isDefault())
                .version(template.getVersion())
                .originalFileName(template.getOriginalFileName())
                .fileType(template.getFileType())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
    
    private static List<ParagraphStyleDTO> convertParagraphStyles(List<DocumentTemplate.ParagraphStyle> styles) {
        if (styles == null) return null;
        
        return styles.stream()
                .map(style -> ParagraphStyleDTO.builder()
                        .id(style.getId())
                        .name(style.getName())
                        .type(style.getType())
                        .cssClass(style.getCssClass())
                        .cssStyles(style.getCssStyles())
                        .properties(style.getProperties())
                        .isDefault(style.isDefault())
                        .order(style.getOrder())
                        .build())
                .toList();
    }
}




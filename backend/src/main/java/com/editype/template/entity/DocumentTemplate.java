package com.editype.template.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сущность шаблона документа
 * Хранит шаблоны макетов, доступные для создания документов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "document_templates")
public class DocumentTemplate {
    
    @Id
    private String id;
    
    // Основная информация
    private String name;
    private String description;
    private String documentTypeId; // Связь с типом документа
    private String category;
    
    // Контент шаблона
    private String content; // HTML/XML контент шаблона
    private Map<String, String> metadata; // Метаданные шаблона
    
    // Стили и форматирование
    private String cssStyles; // CSS стили для шаблона
    private List<ParagraphStyle> paragraphStyles; // Стили абзацев
    
    // Настройки
    private boolean active;
    private boolean isDefault; // Шаблон по умолчанию для типа документа
    private String version;
    
    // Файлы шаблона
    private String originalFileName; // Исходное имя файла
    private String fileType; // Тип файла (html, docx, latex)
    private String filePath; // Путь к файлу в файловой системе
    
    // Автор и права
    private String createdBy; // ID пользователя, создавшего шаблон
    private String lastModifiedBy; // ID пользователя, последним изменившего шаблон
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Вложенный класс для стилей абзацев
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParagraphStyle {
        private String id; // Уникальный ID стиля
        private String name; // Отображаемое имя
        private String type; // Тип абзаца (heading, paragraph, list, quote, etc.)
        private String cssClass; // CSS класс
        private String cssStyles; // Инлайн CSS стили
        private Map<String, String> properties; // Дополнительные свойства
        private boolean isDefault; // Стиль по умолчанию для типа абзаца
        private int order; // Порядок отображения в списке
    }
}




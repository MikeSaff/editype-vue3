package com.editype.article.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Абзац документа - минимальный элемент структуры
 * Может содержать: текст, формулы, таблицы, изображения
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleParagraph {
    
    private String id; // Уникальный ID абзаца
    private String type; // Тип абзаца из шаблона
    private String styleId; // ID стиля абзаца из шаблона
    private int order; // Порядковый номер в документе
    
    // Контент абзаца
    private String content; // HTML контент абзаца
    private String plainText; // Текстовое представление
    
    // Свойства абзаца
    private Map<String, Object> properties; // Дополнительные свойства
    
    // Специальные элементы
    private ParagraphContentType contentType; // Тип основного контента
    
    /**
     * Типы контента в абзаце
     */
    public enum ParagraphContentType {
        TEXT,           // Обычный текст
        FORMULA_INLINE, // Inline LaTeX формула
        FORMULA_BLOCK,  // Block LaTeX формула
        TABLE,          // Таблица
        IMAGE,          // Изображение
        CODE,           // Код
        QUOTE,          // Цитата
        LIST_ITEM       // Элемент списка
    }
}






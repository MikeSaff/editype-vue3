package com.editype.znan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для запроса создания статьи в Znan.io
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZnanCreateArticleRequestDTO {
    
    // Тип документа из Editorum
    private String documentType;
    
    // Шаблон макета
    private String templateId;
    
    // Начальные данные
    private Map<String, String> title;
    private Map<String, String> abstract_;
    private Map<String, String> keywords;
    
    // Журнал/коллекция
    private String journalId;
    
    // Начальный язык
    private String defaultLanguage;
    
    // Создать пустой документ или загрузить файл
    private boolean createEmpty;
    private String uploadedFileId; // ID загруженного файла
}




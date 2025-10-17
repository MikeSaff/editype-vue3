package com.editype.template.service;

import com.editype.template.dto.TemplateCreateRequestDTO;
import com.editype.template.dto.TemplateResponseDTO;
import com.editype.template.entity.DocumentTemplate;
import com.editype.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для работы с шаблонами документов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final TemplateRepository templateRepository;
    private static final String TEMPLATE_UPLOAD_DIR = "uploads/templates/";
    
    /**
     * Получение всех активных шаблонов
     */
    public List<TemplateResponseDTO> getAllActiveTemplates() {
        List<DocumentTemplate> templates = templateRepository.findByActiveTrueOrderByCreatedAtDesc();
        return templates.stream()
                .map(TemplateResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Получение шаблонов по типу документа
     */
    public List<TemplateResponseDTO> getTemplatesByDocumentType(String documentTypeId) {
        List<DocumentTemplate> templates = templateRepository.findByDocumentTypeIdAndActiveTrue(documentTypeId);
        return templates.stream()
                .map(TemplateResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Получение шаблона по умолчанию для типа документа
     */
    public TemplateResponseDTO getDefaultTemplateForDocumentType(String documentTypeId) {
        DocumentTemplate template = templateRepository.findByDocumentTypeIdAndIsDefaultTrueAndActiveTrue(documentTypeId)
                .orElse(null);
        return template != null ? TemplateResponseDTO.fromEntity(template) : null;
    }
    
    /**
     * Получение шаблона по ID
     */
    public TemplateResponseDTO getTemplateById(String id) {
        DocumentTemplate template = templateRepository.findById(id).orElse(null);
        return template != null ? TemplateResponseDTO.fromEntity(template) : null;
    }
    
    /**
     * Создание нового шаблона
     */
    public TemplateResponseDTO createTemplate(TemplateCreateRequestDTO request, String createdBy) {
        try {
            DocumentTemplate template = DocumentTemplate.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .documentTypeId(request.getDocumentTypeId())
                    .category(request.getCategory())
                    .content(request.getContent())
                    .metadata(request.getMetadata())
                    .cssStyles(request.getCssStyles())
                    .paragraphStyles(convertParagraphStyles(request.getParagraphStyles()))
                    .active(request.isActive())
                    .isDefault(request.isDefault())
                    .version(request.getVersion() != null ? request.getVersion() : "1.0")
                    .createdBy(createdBy)
                    .lastModifiedBy(createdBy)
                    .build();
            
            // Обработка загруженного файла
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                String fileName = saveUploadedFile(request.getFile());
                template.setOriginalFileName(request.getFile().getOriginalFilename());
                template.setFileType(request.getFileType());
                template.setFilePath(fileName);
            }
            
            // Если это шаблон по умолчанию, снимаем флаг с других
            if (request.isDefault() && request.getDocumentTypeId() != null) {
                unsetDefaultForDocumentType(request.getDocumentTypeId());
            }
            
            DocumentTemplate savedTemplate = templateRepository.save(template);
            log.info("Created template: {} by user: {}", savedTemplate.getId(), createdBy);
            
            return TemplateResponseDTO.fromEntity(savedTemplate);
            
        } catch (Exception e) {
            log.error("Error creating template", e);
            throw new RuntimeException("Ошибка создания шаблона", e);
        }
    }
    
    /**
     * Обновление шаблона
     */
    public TemplateResponseDTO updateTemplate(String id, TemplateCreateRequestDTO request, String modifiedBy) throws java.io.IOException {
        DocumentTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Шаблон не найден"));
        
        // Обновляем поля
        existingTemplate.setName(request.getName());
        existingTemplate.setDescription(request.getDescription());
        existingTemplate.setContent(request.getContent());
        existingTemplate.setMetadata(request.getMetadata());
        existingTemplate.setCssStyles(request.getCssStyles());
        existingTemplate.setParagraphStyles(convertParagraphStyles(request.getParagraphStyles()));
        existingTemplate.setActive(request.isActive());
        existingTemplate.setLastModifiedBy(modifiedBy);
        existingTemplate.setUpdatedAt(LocalDateTime.now());
        
        // Обработка нового файла
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String fileName = saveUploadedFile(request.getFile());
            existingTemplate.setOriginalFileName(request.getFile().getOriginalFilename());
            existingTemplate.setFileType(request.getFileType());
            existingTemplate.setFilePath(fileName);
        }
        
        // Если это шаблон по умолчанию, снимаем флаг с других
        if (request.isDefault() && existingTemplate.getDocumentTypeId() != null) {
            unsetDefaultForDocumentType(existingTemplate.getDocumentTypeId());
        }
        existingTemplate.setDefault(request.isDefault());
        
        DocumentTemplate savedTemplate = templateRepository.save(existingTemplate);
        log.info("Updated template: {} by user: {}", savedTemplate.getId(), modifiedBy);
        
        return TemplateResponseDTO.fromEntity(savedTemplate);
    }
    
    /**
     * Удаление шаблона (мягкое удаление)
     */
    public void deleteTemplate(String id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Шаблон не найден"));
        
        template.setActive(false);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
        
        log.info("Deleted template: {}", id);
    }
    
    /**
     * Поиск шаблонов по имени
     */
    public List<TemplateResponseDTO> searchTemplatesByName(String name) {
        List<DocumentTemplate> templates = templateRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
        return templates.stream()
                .map(TemplateResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Сохранение загруженного файла
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // Создаем директорию если не существует
        Path uploadDir = Paths.get(TEMPLATE_UPLOAD_DIR);
        Files.createDirectories(uploadDir);
        
        // Генерируем уникальное имя файла
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Сохраняем файл
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
    /**
     * Снятие флага "по умолчанию" с других шаблонов типа документа
     */
    private void unsetDefaultForDocumentType(String documentTypeId) {
        List<DocumentTemplate> templates = templateRepository.findByDocumentTypeIdAndActiveTrue(documentTypeId);
        templates.forEach(template -> {
            if (template.isDefault()) {
                template.setDefault(false);
                templateRepository.save(template);
            }
        });
    }
    
    /**
     * Конвертация DTO стилей абзацев в сущности
     */
    private List<DocumentTemplate.ParagraphStyle> convertParagraphStyles(
            List<TemplateCreateRequestDTO.ParagraphStyleRequestDTO> styleDTOs) {
        if (styleDTOs == null) return null;
        
        return styleDTOs.stream()
                .map(dto -> DocumentTemplate.ParagraphStyle.builder()
                        .id(UUID.randomUUID().toString())
                        .name(dto.getName())
                        .type(dto.getType())
                        .cssClass(dto.getCssClass())
                        .cssStyles(dto.getCssStyles())
                        .properties(dto.getProperties())
                        .isDefault(dto.isDefault())
                        .order(dto.getOrder())
                        .build())
                .collect(Collectors.toList());
    }
}




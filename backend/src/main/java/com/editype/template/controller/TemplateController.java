package com.editype.template.controller;

import com.editype.template.dto.TemplateCreateRequestDTO;
import com.editype.template.dto.TemplateResponseDTO;
import com.editype.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с шаблонами документов
 */
@Slf4j
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemplateController {
    
    private final TemplateService templateService;
    
    /**
     * Получение всех активных шаблонов
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates() {
        List<TemplateResponseDTO> templates = templateService.getAllActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Получение шаблонов по типу документа
     */
    @GetMapping("/by-document-type/{documentTypeId}")
    public ResponseEntity<List<TemplateResponseDTO>> getTemplatesByDocumentType(
            @PathVariable String documentTypeId) {
        List<TemplateResponseDTO> templates = templateService.getTemplatesByDocumentType(documentTypeId);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Получение шаблона по умолчанию для типа документа
     */
    @GetMapping("/default/{documentTypeId}")
    public ResponseEntity<TemplateResponseDTO> getDefaultTemplate(
            @PathVariable String documentTypeId) {
        TemplateResponseDTO template = templateService.getDefaultTemplateForDocumentType(documentTypeId);
        if (template != null) {
            return ResponseEntity.ok(template);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Получение шаблона по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(@PathVariable String id) {
        TemplateResponseDTO template = templateService.getTemplateById(id);
        if (template != null) {
            return ResponseEntity.ok(template);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Создание нового шаблона (только для админов)
     */
    @PostMapping
    public ResponseEntity<TemplateResponseDTO> createTemplate(
            @ModelAttribute TemplateCreateRequestDTO request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TemplateResponseDTO template = templateService.createTemplate(request, userEmail);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Error creating template", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Обновление шаблона (только для админов)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponseDTO> updateTemplate(
            @PathVariable String id,
            @ModelAttribute TemplateCreateRequestDTO request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TemplateResponseDTO template = templateService.updateTemplate(id, request, userEmail);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Error updating template {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Удаление шаблона (только для админов)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting template {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Поиск шаблонов по имени
     */
    @GetMapping("/search")
    public ResponseEntity<List<TemplateResponseDTO>> searchTemplates(
            @RequestParam String name) {
        List<TemplateResponseDTO> templates = templateService.searchTemplatesByName(name);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Импорт документа (HTML, LaTeX, DOCX, ZIP) как шаблона
     */
    @PostMapping("/import")
    public ResponseEntity<java.util.Map<String, Object>> importDocumentAsTemplate(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "documentTypeId", required = false) String documentTypeId
    ) {
        log.info("POST /api/templates/import - file: {}", file.getOriginalFilename());

        // TODO: Inject FileImportService and use it here
        // For now, return a placeholder response

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", false);
        response.put("error", "File import service not yet wired. Please update controller dependencies.");
        
        return ResponseEntity.badRequest().body(response);
    }
}
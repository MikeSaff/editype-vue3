package com.editype.znan.controller;

import com.editype.auth.service.AuthService;
import com.editype.editorum.client.EditorumApiClient;
import com.editype.editorum.dto.EditorumDocumentTypeDTO;
import com.editype.znan.dto.ZnanDocumentTypeResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с типами документов
 */
@Slf4j
@RestController
@RequestMapping("/api/znan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentTypesController {
    
    private final AuthService authService;
    private final EditorumApiClient editorumApiClient;
    
    /**
     * Получение доступных типов документов
     */
    @GetMapping("/document-types")
    public ResponseEntity<List<ZnanDocumentTypeResponseDTO>> getDocumentTypes(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Loading document types for user: {}", userEmail);
            
            // Получаем токен Editorum для текущего пользователя
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                log.warn("No Editorum token found for user: {}", userEmail);
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Загружаем типы документов из Editorum API
            List<EditorumDocumentTypeDTO> editorumTypes = editorumApiClient.getDocumentTypes(
                "Bearer " + editorumToken
            );
            
            // Конвертируем в формат Znan.io
            List<ZnanDocumentTypeResponseDTO> documentTypes = editorumTypes.stream()
                .map(this::convertToZnanDocumentType)
                .collect(Collectors.toList());
            
            log.info("Loaded {} document types for user: {}", documentTypes.size(), userEmail);
            return ResponseEntity.ok(documentTypes);
            
        } catch (Exception e) {
            log.error("Error loading document types", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получение типа документа по ID
     */
    @GetMapping("/document-types/{id}")
    public ResponseEntity<ZnanDocumentTypeResponseDTO> getDocumentType(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Loading document type {} for user: {}", id, userEmail);
            
            // Получаем токен Editorum
            String editorumToken = authService.getEditorumAccessToken(userEmail);
            if (editorumToken == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(null);
            }
            
            // Загружаем тип документа из Editorum API
            EditorumDocumentTypeDTO editorumType = editorumApiClient.getDocumentType(
                "Bearer " + editorumToken, id
            );
            
            // Конвертируем в формат Znan.io
            ZnanDocumentTypeResponseDTO documentType = convertToZnanDocumentType(editorumType);
            
            return ResponseEntity.ok(documentType);
            
        } catch (Exception e) {
            log.error("Error loading document type {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Конвертация EditorumDocumentTypeDTO в ZnanDocumentTypeResponseDTO
     */
    private ZnanDocumentTypeResponseDTO convertToZnanDocumentType(EditorumDocumentTypeDTO editorumType) {
        return ZnanDocumentTypeResponseDTO.builder()
                .id(editorumType.getId())
                .name(editorumType.getName())
                .description(editorumType.getDescription())
                .category(editorumType.getCategory())
                .active(editorumType.isActive())
                .availableTemplates(editorumType.getAvailableTemplates())
                .defaultTemplateId(editorumType.getDefaultTemplateId())
                .defaultLanguage(editorumType.getDefaultLanguage())
                .supportedLanguages(editorumType.getSupportedLanguages())
                .build();
    }
}




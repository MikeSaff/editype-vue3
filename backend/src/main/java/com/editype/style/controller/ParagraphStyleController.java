package com.editype.style.controller;

import com.editype.style.dto.ParagraphStyleRequestDTO;
import com.editype.style.dto.ParagraphStyleResponseDTO;
import com.editype.style.service.ParagraphStyleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/styles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ParagraphStyleController {

    private final ParagraphStyleService styleService;

    /**
     * Get all paragraph styles
     */
    @GetMapping
    public ResponseEntity<List<ParagraphStyleResponseDTO>> getAllStyles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean userSelectable
    ) {
        log.info("GET /api/admin/styles - category: {}, userSelectable: {}", category, userSelectable);

        List<ParagraphStyleResponseDTO> styles;
        
        if (category != null) {
            styles = styleService.getStylesByCategory(category);
        } else if (Boolean.TRUE.equals(userSelectable)) {
            styles = styleService.getUserSelectableStyles();
        } else {
            styles = styleService.getAllStyles();
        }

        return ResponseEntity.ok(styles);
    }

    /**
     * Get style by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParagraphStyleResponseDTO> getStyleById(@PathVariable String id) {
        log.info("GET /api/admin/styles/{}", id);
        ParagraphStyleResponseDTO style = styleService.getStyleById(id);
        return ResponseEntity.ok(style);
    }

    /**
     * Create new paragraph style
     */
    @PostMapping
    public ResponseEntity<ParagraphStyleResponseDTO> createStyle(@RequestBody ParagraphStyleRequestDTO requestDTO) {
        log.info("POST /api/admin/styles - name: {}", requestDTO.getName());
        ParagraphStyleResponseDTO style = styleService.createStyle(requestDTO);
        return ResponseEntity.ok(style);
    }

    /**
     * Update paragraph style
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParagraphStyleResponseDTO> updateStyle(
            @PathVariable String id,
            @RequestBody ParagraphStyleRequestDTO requestDTO
    ) {
        log.info("PUT /api/admin/styles/{}", id);
        ParagraphStyleResponseDTO style = styleService.updateStyle(id, requestDTO);
        return ResponseEntity.ok(style);
    }

    /**
     * Delete paragraph style
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStyle(@PathVariable String id) {
        log.info("DELETE /api/admin/styles/{}", id);
        styleService.deleteStyle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Initialize default styles (for development/testing)
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaults() {
        log.info("POST /api/admin/styles/initialize-defaults");
        styleService.initializeDefaultStyles();
        return ResponseEntity.ok("Default styles initialized");
    }
}



package com.editype.style.service;

import com.editype.style.dto.ParagraphStyleRequestDTO;
import com.editype.style.dto.ParagraphStyleResponseDTO;
import com.editype.style.entity.ParagraphStyle;
import com.editype.style.repository.ParagraphStyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParagraphStyleService {

    private final ParagraphStyleRepository styleRepository;

    /**
     * Get all paragraph styles
     */
    public List<ParagraphStyleResponseDTO> getAllStyles() {
        return styleRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get styles by category
     */
    public List<ParagraphStyleResponseDTO> getStylesByCategory(String category) {
        return styleRepository.findByCategory(category)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user-selectable styles
     */
    public List<ParagraphStyleResponseDTO> getUserSelectableStyles() {
        return styleRepository.findByIsUserSelectable(true)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get style by ID
     */
    public ParagraphStyleResponseDTO getStyleById(String id) {
        ParagraphStyle style = styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Style not found: " + id));
        return toResponseDTO(style);
    }

    /**
     * Create new paragraph style
     */
    public ParagraphStyleResponseDTO createStyle(ParagraphStyleRequestDTO requestDTO) {
        log.info("Creating new paragraph style: {}", requestDTO.getName());

        ParagraphStyle style = ParagraphStyle.builder()
                .name(requestDTO.getName())
                .type(requestDTO.getType())
                .description(requestDTO.getDescription())
                .cssProperties(requestDTO.getCssProperties() != null ? requestDTO.getCssProperties() : new HashMap<>())
                .htmlTag(requestDTO.getHtmlTag())
                .isUserSelectable(requestDTO.getIsUserSelectable() != null ? requestDTO.getIsUserSelectable() : true)
                .isSystemStyle(false) // User-created styles are never system styles
                .category(requestDTO.getCategory())
                .iconUrl(requestDTO.getIconUrl())
                .build();

        style = styleRepository.save(style);
        log.info("Created paragraph style with ID: {}", style.getId());

        return toResponseDTO(style);
    }

    /**
     * Update existing paragraph style
     */
    public ParagraphStyleResponseDTO updateStyle(String id, ParagraphStyleRequestDTO requestDTO) {
        log.info("Updating paragraph style: {}", id);

        ParagraphStyle style = styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Style not found: " + id));

        // Don't allow updating system styles
        if (Boolean.TRUE.equals(style.getIsSystemStyle())) {
            throw new RuntimeException("Cannot update system style: " + id);
        }

        style.setName(requestDTO.getName());
        style.setType(requestDTO.getType());
        style.setDescription(requestDTO.getDescription());
        style.setCssProperties(requestDTO.getCssProperties());
        style.setHtmlTag(requestDTO.getHtmlTag());
        style.setIsUserSelectable(requestDTO.getIsUserSelectable());
        style.setCategory(requestDTO.getCategory());
        style.setIconUrl(requestDTO.getIconUrl());

        style = styleRepository.save(style);
        log.info("Updated paragraph style: {}", id);

        return toResponseDTO(style);
    }

    /**
     * Delete paragraph style
     */
    public void deleteStyle(String id) {
        log.info("Deleting paragraph style: {}", id);

        ParagraphStyle style = styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Style not found: " + id));

        // Don't allow deleting system styles
        if (Boolean.TRUE.equals(style.getIsSystemStyle())) {
            throw new RuntimeException("Cannot delete system style: " + id);
        }

        styleRepository.delete(style);
        log.info("Deleted paragraph style: {}", id);
    }

    /**
     * Initialize default system styles
     */
    public void initializeDefaultStyles() {
        log.info("Initializing default paragraph styles");

        // Check if styles already exist
        if (styleRepository.count() > 0) {
            log.info("Styles already initialized");
            return;
        }

        // Create default styles
        createDefaultStyle("Заголовок 1", "heading1", "h1", "headings", true,
                createCssProperties("24px", "bold", "16px", "8px"));
        createDefaultStyle("Заголовок 2", "heading2", "h2", "headings", true,
                createCssProperties("20px", "bold", "14px", "6px"));
        createDefaultStyle("Заголовок 3", "heading3", "h3", "headings", true,
                createCssProperties("18px", "bold", "12px", "4px"));
        createDefaultStyle("Обычный текст", "paragraph", "p", "text", true,
                createCssProperties("14px", "normal", "8px", "8px"));
        createDefaultStyle("Цитата", "quote", "blockquote", "special", true,
                createCssProperties("14px", "italic", "16px", "16px"));
        createDefaultStyle("Код", "code", "pre", "special", true,
                createCssProperties("12px", "normal", "8px", "8px"));

        log.info("Default paragraph styles initialized");
    }

    private void createDefaultStyle(String name, String type, String htmlTag, String category,
                                     boolean userSelectable, HashMap<String, String> cssProperties) {
        ParagraphStyle style = ParagraphStyle.builder()
                .name(name)
                .type(type)
                .htmlTag(htmlTag)
                .category(category)
                .isUserSelectable(userSelectable)
                .isSystemStyle(true)
                .cssProperties(cssProperties)
                .description("Системный стиль: " + name)
                .build();

        styleRepository.save(style);
    }

    private HashMap<String, String> createCssProperties(String fontSize, String fontWeight,
                                                          String marginTop, String marginBottom) {
        HashMap<String, String> props = new HashMap<>();
        props.put("font-size", fontSize);
        props.put("font-weight", fontWeight);
        props.put("margin-top", marginTop);
        props.put("margin-bottom", marginBottom);
        return props;
    }

    private ParagraphStyleResponseDTO toResponseDTO(ParagraphStyle style) {
        return ParagraphStyleResponseDTO.builder()
                .id(style.getId())
                .name(style.getName())
                .type(style.getType())
                .description(style.getDescription())
                .cssProperties(style.getCssProperties())
                .htmlTag(style.getHtmlTag())
                .isUserSelectable(style.getIsUserSelectable())
                .isSystemStyle(style.getIsSystemStyle())
                .category(style.getCategory())
                .iconUrl(style.getIconUrl())
                .createdAt(style.getCreatedAt())
                .updatedAt(style.getUpdatedAt())
                .build();
    }
}



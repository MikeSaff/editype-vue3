package com.editype.style.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a paragraph style that can be applied to paragraphs in documents.
 * Styles define the visual appearance and formatting rules for different types of paragraphs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "paragraph_styles")
public class ParagraphStyle {

    @Id
    private String id;

    private String name; // Display name (e.g., "Heading 1", "Body Text", "Quote")
    private String type; // Technical type (e.g., "heading1", "paragraph", "quote", "list_item")
    private String description;

    // CSS properties for the style
    @Builder.Default
    private Map<String, String> cssProperties = new HashMap<>();
    // Example: {"font-size": "24px", "font-weight": "bold", "margin-bottom": "12px"}

    // HTML tag to use (e.g., "h1", "p", "blockquote")
    private String htmlTag;

    // Whether this style can be selected by users
    private Boolean isUserSelectable;

    // Whether this is a system style (cannot be deleted)
    private Boolean isSystemStyle;

    // Category for grouping styles (e.g., "headings", "text", "special")
    private String category;

    // Icon or visual indicator for the style
    private String iconUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}



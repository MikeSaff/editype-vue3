package com.editype.style.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphStyleResponseDTO {

    private String id;
    private String name;
    private String type;
    private String description;
    private Map<String, String> cssProperties;
    private String htmlTag;
    private Boolean isUserSelectable;
    private Boolean isSystemStyle;
    private String category;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



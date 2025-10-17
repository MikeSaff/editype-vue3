package com.editype.publication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for creating/updating publication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationRequestDTO {
    
    @NotNull(message = "Titles are required")
    private Map<String, String> titles;
    
    private Map<String, Map<String, String>> metadata;
    
    private Map<String, String> texts;
    
    private String doi;
}





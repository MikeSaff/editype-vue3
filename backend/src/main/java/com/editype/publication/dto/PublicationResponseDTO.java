package com.editype.publication.dto;

import com.editype.publication.entity.Publication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for returning publication data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationResponseDTO {
    
    private String id;
    private Map<String, String> titles;
    private Map<String, Map<String, String>> metadata;
    private Map<String, String> texts;
    private String doi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Publication entity to ResponseDTO
     */
    public static PublicationResponseDTO fromEntity(Publication publication) {
        return PublicationResponseDTO.builder()
                .id(publication.getId())
                .titles(publication.getTitles())
                .metadata(publication.getMetadata())
                .texts(publication.getTexts())
                .doi(publication.getDoi())
                .createdAt(publication.getCreatedAt())
                .updatedAt(publication.getUpdatedAt())
                .build();
    }
}





package com.editype.editorum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO matching Editorum API article structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorumArticleDTO {
    
    private Long id;
    private Boolean isMaterial;
    
    // Metadata by language
    private Map<String, MetadataFields> ru;
    private Map<String, MetadataFields> en;
    
    // Single text field (as in Editorum!)
    private String text;
    
    // Authors (array of IDs in Editorum)
    private List<Long> authors;
    
    // References
    private List<ReferenceDTO> references;
    
    // Rubric
    private RubricDTO rubric;
    
    // DOI
    private String doi;
    private String doiUrl;
    private String rincUrl;
    
    // Pages
    private Integer firstPage;
    private Integer lastPage;
    private Integer authorPages;
    
    // Statuses
    private StatusesDTO statuses;
    
    // Languages
    private List<String> languages;
    
    // Comment
    private String comment;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataFields {
        private String title;
        private String annotation;
        private String keywords;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceDTO {
        private Long id;
        private Map<String, TextDTO> ru;
        private Map<String, TextDTO> en;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextDTO {
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RubricDTO {
        private Long id;
        private Map<String, String> ru;
        private Map<String, String> en;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusesDTO {
        private Integer status;
        private Integer isPublished;
        private Boolean hasPublished;
        private Boolean hasReviewers;
    }
}





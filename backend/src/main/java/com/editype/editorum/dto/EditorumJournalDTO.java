package com.editype.editorum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Editorum journal
 * GET /rest/api/journals/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorumJournalDTO {
    
    private Long id;
    private String doi;
    private String cover;
    private Long rincId;
    private String issnPrint;
    private String issnOnline;
    
    // Multilingual fields
    private Map<String, JournalMetadata> ru;
    private Map<String, JournalMetadata> en;
    
    // Languages supported
    private List<String> languages;
    
    // Rubrics
    private List<RubricDTO> rubrics;
    
    // Issues
    private List<IssueDTO> issues;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalMetadata {
        private String title;
        private String about;
        private String requirementsForArticles;
        private String ethicsPublications;
        private String orderOfPublication;
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
    public static class IssueDTO {
        private Long id;
        private String volume;
        private Integer issue;
        private Integer pageCount;
        private String datePublished;
    }
}





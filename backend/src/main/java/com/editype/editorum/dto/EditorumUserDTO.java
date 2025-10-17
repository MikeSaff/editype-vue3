package com.editype.editorum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Editorum user profile
 * GET /rest/api/user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorumUserDTO {
    
    private Long id;
    private String email;
    private AuthorDTO author;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDTO {
        private Long id;
        private Map<String, String> ru; // {fio: "..."}
        private Map<String, String> en; // {fio: "..."}
        private Object country;
        private Object workplaces;
    }
}





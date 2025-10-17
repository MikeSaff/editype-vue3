package com.editype.publication.controller;

import com.editype.publication.dto.PublicationRequestDTO;
import com.editype.publication.dto.PublicationResponseDTO;
import com.editype.publication.service.PublicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Publication operations
 */
@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicationController {
    
    private final PublicationService publicationService;
    
    /**
     * Get all publications
     */
    @GetMapping
    public ResponseEntity<List<PublicationResponseDTO>> getAllPublications() {
        return ResponseEntity.ok(publicationService.getAllPublications());
    }
    
    /**
     * Get publication by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponseDTO> getPublicationById(@PathVariable String id) {
        return ResponseEntity.ok(publicationService.getPublicationById(id));
    }
    
    /**
     * Create new publication
     */
    @PostMapping
    public ResponseEntity<PublicationResponseDTO> createPublication(@Valid @RequestBody PublicationRequestDTO requestDTO) {
        PublicationResponseDTO createdPublication = publicationService.createPublication(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPublication);
    }
    
    /**
     * Update existing publication
     */
    @PutMapping("/{id}")
    public ResponseEntity<PublicationResponseDTO> updatePublication(
            @PathVariable String id,
            @Valid @RequestBody PublicationRequestDTO requestDTO) {
        return ResponseEntity.ok(publicationService.updatePublication(id, requestDTO));
    }
    
    /**
     * Delete publication
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable String id) {
        publicationService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }
}





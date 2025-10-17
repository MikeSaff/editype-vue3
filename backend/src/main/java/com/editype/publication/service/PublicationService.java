package com.editype.publication.service;

import com.editype.exception.ResourceNotFoundException;
import com.editype.publication.dto.PublicationRequestDTO;
import com.editype.publication.dto.PublicationResponseDTO;
import com.editype.publication.entity.Publication;
import com.editype.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Publication operations
 */
@Service
@RequiredArgsConstructor
public class PublicationService {
    
    private final PublicationRepository publicationRepository;
    
    /**
     * Get all publications
     */
    public List<PublicationResponseDTO> getAllPublications() {
        return publicationRepository.findAll()
                .stream()
                .map(PublicationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get publication by ID
     */
    public PublicationResponseDTO getPublicationById(String id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found with id: " + id));
        return PublicationResponseDTO.fromEntity(publication);
    }
    
    /**
     * Create new publication
     */
    public PublicationResponseDTO createPublication(PublicationRequestDTO requestDTO) {
        // Check if DOI already exists
        if (requestDTO.getDoi() != null && publicationRepository.existsByDoi(requestDTO.getDoi())) {
            throw new IllegalArgumentException("Publication with DOI " + requestDTO.getDoi() + " already exists");
        }
        
        Publication publication = Publication.builder()
                .titles(requestDTO.getTitles())
                .metadata(requestDTO.getMetadata())
                .texts(requestDTO.getTexts())
                .doi(requestDTO.getDoi())
                .build();
        
        Publication savedPublication = publicationRepository.save(publication);
        return PublicationResponseDTO.fromEntity(savedPublication);
    }
    
    /**
     * Update existing publication
     */
    public PublicationResponseDTO updatePublication(String id, PublicationRequestDTO requestDTO) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found with id: " + id));
        
        // Update DOI if changed and not already taken
        if (requestDTO.getDoi() != null && !requestDTO.getDoi().equals(publication.getDoi())) {
            if (publicationRepository.existsByDoi(requestDTO.getDoi())) {
                throw new IllegalArgumentException("DOI " + requestDTO.getDoi() + " is already in use");
            }
            publication.setDoi(requestDTO.getDoi());
        }
        
        // Update multilingual fields
        if (requestDTO.getTitles() != null) {
            publication.setTitles(requestDTO.getTitles());
        }
        if (requestDTO.getMetadata() != null) {
            publication.setMetadata(requestDTO.getMetadata());
        }
        if (requestDTO.getTexts() != null) {
            publication.setTexts(requestDTO.getTexts());
        }
        
        Publication updatedPublication = publicationRepository.save(publication);
        return PublicationResponseDTO.fromEntity(updatedPublication);
    }
    
    /**
     * Delete publication by ID
     */
    public void deletePublication(String id) {
        if (!publicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Publication not found with id: " + id);
        }
        publicationRepository.deleteById(id);
    }
}





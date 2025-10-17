package com.editype.publication.repository;

import com.editype.publication.entity.Publication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicationRepository extends MongoRepository<Publication, String> {
    
    /**
     * Find publication by DOI
     */
    Optional<Publication> findByDoi(String doi);
    
    /**
     * Check if publication exists by DOI
     */
    boolean existsByDoi(String doi);
}





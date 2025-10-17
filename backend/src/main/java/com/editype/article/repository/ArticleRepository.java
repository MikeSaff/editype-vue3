package com.editype.article.repository;

import com.editype.article.entity.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Article entity
 */
@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {
    
    /**
     * Find article by Editorum ID
     */
    Optional<Article> findByEditorumId(Long editorumId);
    
    /**
     * Find article by DOI
     */
    Optional<Article> findByDoi(String doi);
    
    /**
     * Check if article exists by DOI
     */
    boolean existsByDoi(String doi);
    
    /**
     * Check if article exists by Editorum ID
     */
    boolean existsByEditorumId(Long editorumId);
}





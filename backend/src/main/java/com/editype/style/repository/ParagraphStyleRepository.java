package com.editype.style.repository;

import com.editype.style.entity.ParagraphStyle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParagraphStyleRepository extends MongoRepository<ParagraphStyle, String> {

    /**
     * Find style by technical type
     */
    Optional<ParagraphStyle> findByType(String type);

    /**
     * Find all styles in a category
     */
    List<ParagraphStyle> findByCategory(String category);

    /**
     * Find all user-selectable styles
     */
    List<ParagraphStyle> findByIsUserSelectable(Boolean isUserSelectable);

    /**
     * Find all non-system styles (can be deleted)
     */
    List<ParagraphStyle> findByIsSystemStyle(Boolean isSystemStyle);
}





package com.editype.template.repository;

import com.editype.template.entity.DocumentTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с шаблонами документов
 */
@Repository
public interface TemplateRepository extends MongoRepository<DocumentTemplate, String> {
    
    /**
     * Поиск активных шаблонов по типу документа
     */
    List<DocumentTemplate> findByDocumentTypeIdAndActiveTrue(String documentTypeId);
    
    /**
     * Поиск шаблона по умолчанию для типа документа
     */
    Optional<DocumentTemplate> findByDocumentTypeIdAndIsDefaultTrueAndActiveTrue(String documentTypeId);
    
    /**
     * Поиск шаблонов по категории
     */
    List<DocumentTemplate> findByCategoryAndActiveTrue(String category);
    
    /**
     * Поиск шаблонов по имени (с поддержкой поиска)
     */
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'active': true}")
    List<DocumentTemplate> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    
    /**
     * Поиск всех активных шаблонов
     */
    List<DocumentTemplate> findByActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Поиск шаблонов, созданных пользователем
     */
    List<DocumentTemplate> findByCreatedByAndActiveTrueOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Проверка существования шаблона с именем
     */
    boolean existsByNameAndActiveTrue(String name);
    
    /**
     * Проверка существования шаблона по умолчанию для типа документа
     */
    boolean existsByDocumentTypeIdAndIsDefaultTrueAndActiveTrue(String documentTypeId);
}




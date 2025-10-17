package com.editype.article.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Multilingual metadata for article (title, annotation, keywords)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMetadata {
    private String title;
    private String annotation;
    private String keywords;
}





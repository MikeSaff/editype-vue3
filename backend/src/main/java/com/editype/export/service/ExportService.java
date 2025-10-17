package com.editype.export.service;

import com.editype.exception.ResourceNotFoundException;
import com.editype.publication.entity.Publication;
import com.editype.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for exporting publications to various formats
 */
@Service
@RequiredArgsConstructor
public class ExportService {
    
    private final PublicationRepository publicationRepository;
    
    /**
     * Export publication to HTML format
     */
    public String exportToHtml(String id, String language) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found with id: " + id));
        
        String title = publication.getTitles().getOrDefault(language, "Untitled");
        String text = publication.getTexts().getOrDefault(language, "No content");
        String doi = publication.getDoi() != null ? publication.getDoi() : "N/A";
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"").append(language).append("\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <title>").append(escapeHtml(title)).append("</title>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: 'Times New Roman', serif; max-width: 800px; margin: 50px auto; line-height: 1.6; }\n");
        html.append("    h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
        html.append("    .metadata { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-left: 4px solid #3498db; }\n");
        html.append("    .content { text-align: justify; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <h1>").append(escapeHtml(title)).append("</h1>\n");
        html.append("  <div class=\"metadata\">\n");
        html.append("    <p><strong>DOI:</strong> ").append(escapeHtml(doi)).append("</p>\n");
        
        // Add metadata if available
        Map<String, String> metadata = publication.getMetadata().get(language);
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                html.append("    <p><strong>").append(escapeHtml(entry.getKey())).append(":</strong> ")
                    .append(escapeHtml(entry.getValue())).append("</p>\n");
            }
        }
        
        html.append("  </div>\n");
        html.append("  <div class=\"content\">\n");
        html.append("    ").append(text).append("\n");
        html.append("  </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Export publication to XML JATS format (simplified)
     */
    public String exportToJatsXml(String id, String language) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found with id: " + id));
        
        String title = publication.getTitles().getOrDefault(language, "Untitled");
        String text = publication.getTexts().getOrDefault(language, "No content");
        String doi = publication.getDoi() != null ? publication.getDoi() : "";
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE article PUBLIC \"-//NLM//DTD JATS (Z39.96) Journal Archiving and Interchange DTD v1.2 20190208//EN\" \"JATS-archivearticle1.dtd\">\n");
        xml.append("<article xmlns:xlink=\"http://www.w3.org/1999/xlink\" article-type=\"research-article\">\n");
        xml.append("  <front>\n");
        xml.append("    <article-meta>\n");
        xml.append("      <title-group>\n");
        xml.append("        <article-title>").append(escapeXml(title)).append("</article-title>\n");
        xml.append("      </title-group>\n");
        
        if (!doi.isEmpty()) {
            xml.append("      <article-id pub-id-type=\"doi\">").append(escapeXml(doi)).append("</article-id>\n");
        }
        
        // Add metadata
        Map<String, String> metadata = publication.getMetadata().get(language);
        if (metadata != null && metadata.containsKey("author")) {
            xml.append("      <contrib-group>\n");
            xml.append("        <contrib contrib-type=\"author\">\n");
            xml.append("          <name><surname>").append(escapeXml(metadata.get("author"))).append("</surname></name>\n");
            xml.append("        </contrib>\n");
            xml.append("      </contrib-group>\n");
        }
        
        xml.append("    </article-meta>\n");
        xml.append("  </front>\n");
        xml.append("  <body>\n");
        xml.append("    <p>").append(escapeXml(text)).append("</p>\n");
        xml.append("  </body>\n");
        xml.append("</article>");
        
        return xml.toString();
    }
    
    /**
     * Export publication to plain text
     */
    public String exportToText(String id, String language) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found with id: " + id));
        
        String title = publication.getTitles().getOrDefault(language, "Untitled");
        String text = publication.getTexts().getOrDefault(language, "No content");
        String doi = publication.getDoi() != null ? publication.getDoi() : "N/A";
        
        StringBuilder txt = new StringBuilder();
        txt.append(title).append("\n");
        txt.append("=".repeat(title.length())).append("\n\n");
        txt.append("DOI: ").append(doi).append("\n\n");
        
        Map<String, String> metadata = publication.getMetadata().get(language);
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                txt.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            txt.append("\n");
        }
        
        txt.append(text);
        
        return txt.toString();
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Escape XML special characters
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}




